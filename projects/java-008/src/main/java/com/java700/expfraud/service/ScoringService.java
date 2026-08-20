package com.java700.expfraud.service;

import com.java700.expfraud.common.audit.AuditLogService;
import com.java700.expfraud.domain.DuplicateGroup;
import com.java700.expfraud.domain.DuplicateGroupRepository;
import com.java700.expfraud.domain.ExpenseClaim;
import com.java700.expfraud.domain.ExpenseClaimRepository;
import com.java700.expfraud.domain.PeerBaseline;
import com.java700.expfraud.domain.PolicyRule;
import com.java700.expfraud.domain.PolicyRuleRepository;
import com.java700.expfraud.domain.RuleViolation;
import com.java700.expfraud.domain.RuleViolationRepository;
import com.java700.expfraud.messaging.DomainEventBus;
import com.java700.expfraud.messaging.FraudEvents;
import com.java700.expfraud.observability.Metrics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The explainable fraud scoring pipeline.
 *
 * <p>Order of detectors (each contributes weighted points and a human-readable reason):</p>
 * <ol>
 *   <li>data-driven policy rules (BLOCKER 45 / VIOLATION 25 / WARNING 10)</li>
 *   <li>weekend-mileage anomaly detector (+20)</li>
 *   <li>peer-pattern outlier detector via z-score against the department baseline (+20/+30)</li>
 *   <li>duplicate / split-receipt clustering via JGraphT connected components (+30/+15)</li>
 * </ol>
 *
 * <p>Scores are clamped to [0,100]; claims at or above the HIGH threshold, or carrying any
 * BLOCKER violation, are routed into the four-eyes case workflow instead of manager approval.</p>
 */
@Service
public class ScoringService {

    public static final int HIGH_THRESHOLD = 65;
    public static final int MEDIUM_THRESHOLD = 35;
    public static final String TIER_HIGH = "HIGH";
    public static final String TIER_MEDIUM = "MEDIUM";
    public static final String TIER_LOW = "LOW";

    private static final int POINTS_BLOCKER = 45;
    private static final int POINTS_VIOLATION = 25;
    private static final int POINTS_WARNING = 10;
    private static final int POINTS_WEEKEND_MILEAGE = 20;
    private static final int POINTS_OUTLIER_STRONG = 30;
    private static final int POINTS_OUTLIER_MILD = 20;
    private static final int POINTS_DUPLICATE = 30;
    private static final int POINTS_SPLIT = 15;

    private static final int DUP_LOOKBACK_DAYS = 30;
    private static final int MIN_PEER_SAMPLES = 5;

    private final PolicyRuleRepository rules;
    private final RuleViolationRepository violations;
    private final DuplicateGroupRepository groups;
    private final ExpenseClaimRepository claims;
    private final BaselineService baselines;
    private final DomainEventBus events;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;
    private final ObjectMapper mapper;

    public ScoringService(PolicyRuleRepository rules, RuleViolationRepository violations,
                          DuplicateGroupRepository groups, ExpenseClaimRepository claims,
                          BaselineService baselines, DomainEventBus events, AuditLogService audit,
                          Metrics metrics, Clock clock, ObjectMapper mapper) {
        this.rules = rules;
        this.violations = violations;
        this.groups = groups;
        this.claims = claims;
        this.baselines = baselines;
        this.events = events;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
        this.mapper = mapper;
    }

    /** Scores a claim and persists violations, clusters and the updated risk score. */
    @Transactional
    public Api.ScoreResult score(ExpenseClaim claim) {
        Instant start = Instant.now(clock);
        List<Api.ScoreReason> reasons = new ArrayList<>();
        boolean blocker = evaluateRules(claim, reasons);
        int score = reasons.stream().mapToInt(Api.ScoreReason::points).sum();
        score += weekendMileage(claim, reasons);
        score += peerOutlier(claim, reasons);
        score += duplicateClustering(claim, reasons);

        int finalScore = Math.min(100, score);
        boolean autoCase = finalScore >= HIGH_THRESHOLD || blocker;
        claim.applyScore(finalScore, toJson(reasons));
        claim.transition(autoCase ? ExpenseClaim.STATUS_UNDER_REVIEW
                                  : ExpenseClaim.STATUS_SCORED);

        metrics.scoringTimer().record(Duration.between(start, Instant.now(clock)));
        metrics.claimScored(finalScore);
        audit.record("CLAIM_SCORED", "expense_claim", claim.getClaimNo(),
                "score=" + finalScore + " tier=" + tierOf(finalScore)
                        + " autoCase=" + autoCase + " reasons=" + reasons.size());
        events.publish(new FraudEvents.ClaimScored(UUID.randomUUID().toString(), Instant.now(clock),
                claim.getId(), claim.getClaimNo(), finalScore, autoCase));
        return new Api.ScoreResult(finalScore, tierOf(finalScore), autoCase, reasons);
    }

    /** Evaluates the active data-driven policy rules against the claim. */
    private boolean evaluateRules(ExpenseClaim claim, List<Api.ScoreReason> reasons) {
        boolean blocker = false;
        for (PolicyRule rule : rules.findByActiveTrueOrderBySortOrderAsc()) {
            if (!matchesCategory(rule, claim.getCategory())) {
                continue;
            }
            RuleHit hit = evaluate(rule, claim);
            if (hit == null) {
                continue;
            }
            int points = pointsFor(rule.getSeverity());
            violations.save(new RuleViolation(UUID.randomUUID().toString(), claim.getId(),
                    rule.getCode(), rule.getMessage(), hit.observed(), hit.expected(),
                    rule.getSeverity(), points, Instant.now(clock)));
            reasons.add(new Api.ScoreReason(rule.getCode(), points, rule.getSeverity(),
                    rule.getMessage()));
            blocker |= PolicyRule.SEVERITY_BLOCKER.equals(rule.getSeverity());
        }
        return blocker;
    }

    private static boolean matchesCategory(PolicyRule rule, String claimCategory) {
        return "ANY".equals(rule.getCategory()) || rule.getCategory().equals(claimCategory);
    }

    private RuleHit evaluate(PolicyRule rule, ExpenseClaim claim) {
        BigDecimal amount = claim.getAmount();
        return switch (rule.getComparator()) {
            case "GREATER_THAN" -> amount.compareTo(rule.getThreshold()) > 0
                    ? new RuleHit("amount=" + amount.toPlainString(),
                                  "<=" + rule.getThreshold().toPlainString())
                    : null;
            case "MISSING_RECEIPT" -> {
                boolean missing = claim.getReceiptRef() == null || claim.getReceiptRef().isBlank();
                boolean aboveFloor = rule.getThreshold() == null
                        || amount.compareTo(rule.getThreshold()) > 0;
                yield missing && aboveFloor ? new RuleHit("receiptRef=absent", "receipt required")
                                            : null;
            }
            case "ROUND_AMOUNT" -> amount.stripTrailingZeros().scale() <= 0
                    && amount.compareTo(BigDecimal.valueOf(50)) >= 0
                    ? new RuleHit("amount=" + amount.toPlainString(), "non-round amount")
                    : null;
            case "MERCHANT_CONTAINS" -> {
                String merchant = claim.getMerchant() == null ? "" : claim.getMerchant();
                boolean match = merchant.toUpperCase()
                        .contains(rule.getPattern().toUpperCase());
                yield match ? new RuleHit("merchant=" + merchant,
                                          "merchant not containing " + rule.getPattern())
                            : null;
            }
            default -> null;
        };
    }

    /** Weekend mileage is a classic fabrication pattern: +20 when mileage lands on Sat/Sun. */
    private int weekendMileage(ExpenseClaim claim, List<Api.ScoreReason> reasons) {
        if (!"MILEAGE".equals(claim.getCategory())) {
            return 0;
        }
        DayOfWeek day = claim.getExpenseDate().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            reasons.add(new Api.ScoreReason("WEEKEND-MILEAGE", POINTS_WEEKEND_MILEAGE, "ANOMALY",
                    "Mileage claimed on a " + day + " — unusual for business travel"));
            return POINTS_WEEKEND_MILEAGE;
        }
        return 0;
    }

    /** Flags claims whose amount is a statistical outlier versus department peers. */
    private int peerOutlier(ExpenseClaim claim, List<Api.ScoreReason> reasons) {
        PeerBaseline baseline = baselines.baselineFor(claim.getDepartment(), claim.getCategory())
                .orElse(null);
        if (baseline == null || baseline.getSampleCount() < MIN_PEER_SAMPLES
                || baseline.getStdDev().compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        BigDecimal z = claim.getAmount().subtract(baseline.getMeanAmount())
                .divide(baseline.getStdDev(), 4, RoundingMode.HALF_UP);
        String zText = z.setScale(2, RoundingMode.HALF_UP).toPlainString();
        if (z.compareTo(BigDecimal.valueOf(3.5)) > 0) {
            reasons.add(new Api.ScoreReason("PEER-OUTLIER", POINTS_OUTLIER_STRONG, "ANOMALY",
                    "Amount is " + zText + " standard deviations above the "
                            + claim.getDepartment() + " " + claim.getCategory() + " peer mean of "
                            + baseline.getMeanAmount().toPlainString()));
            return POINTS_OUTLIER_STRONG;
        }
        if (z.compareTo(BigDecimal.valueOf(2.5)) > 0) {
            reasons.add(new Api.ScoreReason("PEER-OUTLIER", POINTS_OUTLIER_MILD, "ANOMALY",
                    "Amount is " + zText + " standard deviations above the department peer mean"));
            return POINTS_OUTLIER_MILD;
        }
        return 0;
    }

    /**
     * Builds a claim graph (vertices = claims of the same employee sharing a merchant within
     * the look-back window; edges = exact-duplicate or split-receipt similarity) and uses
     * JGraphT connected components to cluster them. Every cluster of size >= 2 becomes an
     * evidence group persisted for the auditor.
     */
    private int duplicateClustering(ExpenseClaim claim, List<Api.ScoreReason> reasons) {
        if (claim.getMerchant() == null || claim.getMerchant().isBlank()) {
            return 0;
        }
        List<ExpenseClaim> pool = candidatePool(claim);
        if (pool.isEmpty()) {
            return 0;
        }
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(claim.getId());
        Map<String, ExpenseClaim> byId = new HashMap<>();
        byId.put(claim.getId(), claim);
        for (ExpenseClaim candidate : pool) {
            graph.addVertex(candidate.getId());
            byId.put(candidate.getId(), candidate);
            if (isExactDuplicate(claim, candidate) || isSplitReceipt(claim, candidate)) {
                graph.addEdge(claim.getId(), candidate.getId());
            }
        }
        for (int i = 0; i < pool.size(); i++) {
            for (int j = i + 1; j < pool.size(); j++) {
                ExpenseClaim a = pool.get(i);
                ExpenseClaim b = pool.get(j);
                if (isExactDuplicate(a, b) || isSplitReceipt(a, b)) {
                    graph.addEdge(a.getId(), b.getId());
                }
            }
        }

        int points = 0;
        ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(graph);
        for (Set<String> component : inspector.connectedSets()) {
            if (component.size() < 2 || !component.contains(claim.getId())) {
                continue;
            }
            boolean exact = componentHasExactPair(component, byId);
            List<String> sortedIds = component.stream().sorted().toList();
            String groupKey = groupKeyOf(sortedIds);
            if (groups.findByGroupKey(groupKey).isEmpty()) {
                BigDecimal confidence = BigDecimal.valueOf(exact ? 0.99 : 0.85);
                groups.save(new DuplicateGroup(UUID.randomUUID().toString(), groupKey,
                        String.join(",", sortedIds), claim.getMerchant(), claim.getAmount(),
                        confidence, component.size(), DuplicateGroup.STATUS_OPEN,
                        Instant.now(clock), null));
                metrics.duplicateGroupCreated();
            }
            if (exact) {
                reasons.add(new Api.ScoreReason("DUPLICATE-CLUSTER", POINTS_DUPLICATE, "ANOMALY",
                        "Matches " + (component.size() - 1) + " near-identical claim(s) at "
                                + claim.getMerchant() + " within " + DUP_LOOKBACK_DAYS + " days"));
                points += POINTS_DUPLICATE;
            } else {
                reasons.add(new Api.ScoreReason("SPLIT-RECEIPT", POINTS_SPLIT, "ANOMALY",
                        "Split receipt pattern at " + claim.getMerchant()
                                + ": same-day receipts that together exceed the policy cap"));
                points += POINTS_SPLIT;
            }
        }
        return points;
    }

    /** Same employee, same merchant, within the look-back window on either side. */
    private List<ExpenseClaim> candidatePool(ExpenseClaim claim) {
        return claims.findByEmployeeIdAndExpenseDateAfter(claim.getEmployeeId(),
                        claim.getExpenseDate().minusDays(DUP_LOOKBACK_DAYS)).stream()
                .filter(other -> !other.getId().equals(claim.getId()))
                .filter(other -> claim.getMerchant().equalsIgnoreCase(other.getMerchant()))
                .filter(other -> !other.getExpenseDate().isAfter(
                        claim.getExpenseDate().plusDays(DUP_LOOKBACK_DAYS)))
                .sorted(Comparator.comparing(ExpenseClaim::getExpenseDate))
                .toList();
    }

    private boolean isExactDuplicate(ExpenseClaim a, ExpenseClaim b) {
        if (!a.getMerchant().equalsIgnoreCase(b.getMerchant())) {
            return false;
        }
        long days = Math.abs(ChronoUnit.DAYS.between(a.getExpenseDate(), b.getExpenseDate()));
        if (days > 14) {
            return false;
        }
        BigDecimal max = a.getAmount().max(b.getAmount());
        BigDecimal diff = a.getAmount().subtract(b.getAmount()).abs();
        BigDecimal tolerance = max.multiply(BigDecimal.valueOf(0.01));
        return diff.compareTo(tolerance) <= 0;
    }

    private boolean isSplitReceipt(ExpenseClaim a, ExpenseClaim b) {
        if (!a.getMerchant().equalsIgnoreCase(b.getMerchant())
                || !a.getExpenseDate().isEqual(b.getExpenseDate())) {
            return false;
        }
        BigDecimal cap = categoryCap(a.getCategory());
        if (cap == null) {
            return false;
        }
        BigDecimal sum = a.getAmount().add(b.getAmount());
        boolean eachBelowCap = a.getAmount().compareTo(cap) < 0
                && b.getAmount().compareTo(cap) < 0;
        return eachBelowCap && sum.compareTo(cap) > 0;
    }

    private BigDecimal categoryCap(String category) {
        return rules.findByActiveTrueOrderBySortOrderAsc().stream()
                .filter(rule -> rule.getCategory().equals(category)
                        && "GREATER_THAN".equals(rule.getComparator()))
                .map(PolicyRule::getThreshold)
                .findFirst()
                .orElse(null);
    }

    private boolean componentHasExactPair(Set<String> component, Map<String, ExpenseClaim> byId) {
        List<String> ids = new ArrayList<>(component);
        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                if (isExactDuplicate(byId.get(ids.get(i)), byId.get(ids.get(j)))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String groupKeyOf(List<String> sortedIds) {
        return sha256(String.join(",", sortedIds)).substring(0, 32);
    }

    private String toJson(List<Api.ScoreReason> reasons) {
        try {
            return mapper.writeValueAsString(reasons);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize score reasons", ex);
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static int pointsFor(String severity) {
        return switch (severity) {
            case PolicyRule.SEVERITY_BLOCKER -> POINTS_BLOCKER;
            case PolicyRule.SEVERITY_VIOLATION -> POINTS_VIOLATION;
            default -> POINTS_WARNING;
        };
    }

    public static String tierOf(int score) {
        if (score >= HIGH_THRESHOLD) {
            return TIER_HIGH;
        }
        if (score >= MEDIUM_THRESHOLD) {
            return TIER_MEDIUM;
        }
        return TIER_LOW;
    }

    private record RuleHit(String observed, String expected) {
    }
}
