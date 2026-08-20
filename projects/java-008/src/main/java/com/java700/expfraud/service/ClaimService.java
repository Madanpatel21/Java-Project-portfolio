package com.java700.expfraud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.expfraud.common.api.Problems;
import com.java700.expfraud.common.audit.AuditLogService;
import com.java700.expfraud.common.web.IdempotencyService;
import com.java700.expfraud.domain.DuplicateGroup;
import com.java700.expfraud.domain.DuplicateGroupRepository;
import com.java700.expfraud.domain.ExpenseClaim;
import com.java700.expfraud.domain.ExpenseClaimRepository;
import com.java700.expfraud.domain.RuleViolation;
import com.java700.expfraud.domain.RuleViolationRepository;
import com.java700.expfraud.messaging.DomainEventBus;
import com.java700.expfraud.messaging.FraudEvents;
import com.java700.expfraud.observability.Metrics;
import com.java700.expfraud.security.Roles;
import com.java700.expfraud.security.SecurityUtil;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Claim intake, review queue and manager approval with policy guards.
 *
 * <p>Managers may approve or reject only low/medium-risk scored claims; anything at HIGH risk
 * or carrying a BLOCKER violation is locked into the four-eyes case workflow.</p>
 */
@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);
    private static final Set<String> CATEGORIES = Set.of(
            "MILEAGE", "MEALS", "LODGING", "SUPPLIES", "ENTERTAINMENT", "OTHER");

    private final ExpenseClaimRepository claims;
    private final RuleViolationRepository violations;
    private final DuplicateGroupRepository groups;
    private final ScoringService scoring;
    private final CaseService cases;
    private final IdempotencyService idempotency;
    private final DomainEventBus events;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;
    private final ObjectMapper mapper;

    public ClaimService(ExpenseClaimRepository claims, RuleViolationRepository violations,
                        DuplicateGroupRepository groups, ScoringService scoring, CaseService cases,
                        IdempotencyService idempotency, DomainEventBus events,
                        AuditLogService audit, Metrics metrics, Clock clock, ObjectMapper mapper) {
        this.claims = claims;
        this.violations = violations;
        this.groups = groups;
        this.scoring = scoring;
        this.cases = cases;
        this.idempotency = idempotency;
        this.events = events;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
        this.mapper = mapper;
    }

    /** Validates, ingests and scores a claim (idempotent by Idempotency-Key). */
    @Transactional
    public ExpenseClaim submit(Api.SubmitClaimRequest request, String idemKey, String username) {
        validate(request);
        if (idemKey != null && !idemKey.isBlank()) {
            String existing = idempotency.begin(idemKey, "claim");
            if (existing != null) {
                return claims.findById(existing)
                        .orElseThrow(() -> new Problems.NotFound("claim " + existing));
            }
        }
        ExpenseClaim claim = new ExpenseClaim(UUID.randomUUID().toString(), nextClaimNo(),
                request.employeeId(), request.employeeName().trim(), request.department().trim(),
                request.category(), request.amount(), request.currency(),
                trimToNull(request.merchant()), request.expenseDate(), trimToNull(request.description()),
                trimToNull(request.receiptRef()), ExpenseClaim.STATUS_SUBMITTED, 0, 0,
                Instant.now(clock), Instant.now(clock));
        ExpenseClaim saved = claims.save(claim);
        Api.ScoreResult result = scoring.score(saved);
        if (result.autoCase()) {
            cases.open(saved, username);
        }
        metrics.claimSubmitted();
        audit.record("CLAIM_SUBMITTED", "expense_claim", saved.getClaimNo(),
                "by=" + username + " category=" + saved.getCategory()
                        + " amount=" + saved.getAmount().toPlainString()
                        + " score=" + result.score());
        events.publish(new FraudEvents.ClaimSubmitted(UUID.randomUUID().toString(),
                Instant.now(clock), saved.getId(), saved.getClaimNo()));
        if (idemKey != null && !idemKey.isBlank()) {
            idempotency.complete(idemKey, saved.getId(), 201);
        }
        return saved;
    }

    /** Manager approval/rejection for low and medium-risk claims only. */
    @Transactional
    public ExpenseClaim decide(String claimId, boolean approve, String note, String username) {
        ExpenseClaim claim = load(claimId);
        if (!ExpenseClaim.STATUS_SCORED.equals(claim.getStatus())) {
            throw new Problems.Conflict("claim " + claim.getClaimNo() + " is "
                    + claim.getStatus() + " and cannot be decided by a manager");
        }
        if (claim.getRiskScore() >= ScoringService.HIGH_THRESHOLD
                || hasBlockerViolation(claim.getId())) {
            throw new Problems.Conflict("claim " + claim.getClaimNo()
                    + " is high-risk and must go through the four-eyes case workflow");
        }
        claim.transition(approve ? ExpenseClaim.STATUS_APPROVED : ExpenseClaim.STATUS_REJECTED);
        ExpenseClaim saved = claims.save(claim);
        audit.record(approve ? "CLAIM_APPROVED" : "CLAIM_REJECTED", "expense_claim",
                saved.getClaimNo(), "by=" + username + " note=" + (note == null ? "" : note));
        return saved;
    }

    @Transactional(readOnly = true)
    public ExpenseClaim load(String claimId) {
        return claims.findById(claimId)
                .orElseThrow(() -> new Problems.NotFound("expense claim " + claimId));
    }

    /** Investigator queue: scored or under-review claims at or above the risk floor. */
    @Transactional(readOnly = true)
    public List<ExpenseClaim> queue(int minScore) {
        return claims.findByStatusInAndRiskScoreGreaterThanEqual(
                        List.of(ExpenseClaim.STATUS_SCORED, ExpenseClaim.STATUS_UNDER_REVIEW),
                        minScore).stream()
                .sorted(Comparator.comparingInt(ExpenseClaim::getRiskScore).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public Api.ClaimView view(ExpenseClaim claim) {
        boolean privileged = SecurityUtil.hasRole(Roles.FRAUD_INVESTIGATOR)
                || SecurityUtil.hasRole(Roles.AUDITOR) || SecurityUtil.hasRole(Roles.ADMIN);
        List<Api.ScoreReason> reasons = parseReasons(claim.getReasonsJson());
        List<Api.ViolationView> claimViolations = violations.findByClaimIdOrderByCreatedAtAsc(
                        claim.getId()).stream()
                .map(this::toViolationView)
                .toList();
        String employeeName = privileged ? claim.getEmployeeName() : MaskingUtil.maskName(
                claim.getEmployeeName());
        return new Api.ClaimView(claim.getId(), claim.getClaimNo(), employeeName,
                claim.getDepartment(), claim.getCategory(), claim.getAmount(), claim.getCurrency(),
                claim.getMerchant(), claim.getExpenseDate(), claim.getDescription(),
                claim.getStatus(), claim.getRiskScore(), ScoringService.tierOf(claim.getRiskScore()),
                claim.getSubmittedAt(), reasons, claimViolations);
    }

    @Transactional(readOnly = true)
    public List<Api.ViolationView> violationsOf(String claimId) {
        return violations.findByClaimIdOrderByCreatedAtAsc(claimId).stream()
                .map(this::toViolationView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Api.DuplicateGroupView> duplicateGroups() {
        return groups.findAll().stream()
                .sorted(Comparator.comparing(DuplicateGroup::getCreatedAt).reversed())
                .map(this::toGroupView)
                .toList();
    }

    private Api.ViolationView toViolationView(RuleViolation violation) {
        return new Api.ViolationView(violation.getRuleCode(), violation.getRuleMessage(),
                violation.getObserved(), violation.getExpected(), violation.getSeverity(),
                violation.getPoints());
    }

    private Api.DuplicateGroupView toGroupView(DuplicateGroup group) {
        List<String> claimNos = Arrays.stream(group.getClaimIds().split(","))
                .map(id -> claims.findById(id).map(ExpenseClaim::getClaimNo).orElse(id))
                .toList();
        return new Api.DuplicateGroupView(group.getId(), group.getGroupKey(), claimNos,
                group.getMerchant(), group.getAmount(), group.getMatchConfidence(),
                group.getGroupSize(), group.getStatus(), group.getCreatedAt());
    }

    private boolean hasBlockerViolation(String claimId) {
        return violations.findByClaimIdOrderByCreatedAtAsc(claimId).stream()
                .anyMatch(v -> "BLOCKER".equals(v.getSeverity()));
    }

    private List<Api.ScoreReason> parseReasons(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return mapper.readValue(json, new TypeReference<List<Api.ScoreReason>>() {
            });
        } catch (JsonProcessingException ex) {
            log.warn("Unparseable reasons JSON on claim", ex);
            return List.of();
        }
    }

    private void validate(Api.SubmitClaimRequest request) {
        if (request.employeeId() == null || request.employeeId().isBlank()) {
            throw new Problems.BadRequest("employeeId is required");
        }
        if (request.employeeName() == null || request.employeeName().isBlank()) {
            throw new Problems.BadRequest("employeeName is required");
        }
        if (request.department() == null || request.department().isBlank()) {
            throw new Problems.BadRequest("department is required");
        }
        if (!CATEGORIES.contains(request.category())) {
            throw new Problems.BadRequest("category must be one of " + CATEGORIES);
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Problems.BadRequest("amount must be positive");
        }
        if (request.currency() == null || !request.currency().matches("[A-Z]{3}")) {
            throw new Problems.BadRequest("currency must be a 3-letter ISO code");
        }
        if (request.expenseDate() == null) {
            throw new Problems.BadRequest("expenseDate is required");
        }
        if (request.expenseDate().isAfter(LocalDate.now(clock).plusDays(1))) {
            throw new Problems.BadRequest("expenseDate cannot be in the future");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private synchronized String nextClaimNo() {
        long count = claims.count();
        return "EF-2026-" + String.format("%05d", count + 1);
    }
}
