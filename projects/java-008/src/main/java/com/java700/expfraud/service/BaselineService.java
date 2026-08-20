package com.java700.expfraud.service;

import com.java700.expfraud.common.audit.AuditLogService;
import com.java700.expfraud.domain.ExpenseClaim;
import com.java700.expfraud.domain.ExpenseClaimRepository;
import com.java700.expfraud.domain.PeerBaseline;
import com.java700.expfraud.domain.PeerBaselineRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes peer spending baselines (mean / median / p90 / std-dev) per
 * department + category from clean claim history, so the scoring engine can
 * flag statistically improbable claims.
 */
@Service
public class BaselineService {

    private static final Logger log = LoggerFactory.getLogger(BaselineService.class);
    private static final List<String> CLEAN_STATUSES =
            List.of(ExpenseClaim.STATUS_SCORED, ExpenseClaim.STATUS_APPROVED);
    private static final int LOOKBACK_DAYS = 180;

    private final ExpenseClaimRepository claims;
    private final PeerBaselineRepository baselines;
    private final AuditLogService audit;
    private final Clock clock;

    public BaselineService(ExpenseClaimRepository claims, PeerBaselineRepository baselines,
                           AuditLogService audit, Clock clock) {
        this.claims = claims;
        this.baselines = baselines;
        this.audit = audit;
        this.clock = clock;
    }

    /** Recomputes every department + category baseline from clean history. */
    @Transactional
    public List<PeerBaseline> recomputeAll() {
        LocalDate after = LocalDate.now(clock).minusDays(LOOKBACK_DAYS);
        Map<String, List<BigDecimal>> buckets = new HashMap<>();
        for (ExpenseClaim claim : claims.findByStatusInOrderBySubmittedAtAsc(CLEAN_STATUSES)) {
            if (claim.getExpenseDate().isBefore(after)) {
                continue;
            }
            buckets.computeIfAbsent(key(claim), ignored -> new ArrayList<>()).add(claim.getAmount());
        }
        List<PeerBaseline> result = new ArrayList<>();
        for (Map.Entry<String, List<BigDecimal>> entry : buckets.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            List<BigDecimal> sorted = entry.getValue().stream().sorted().toList();
            BigDecimal mean = mean(sorted);
            BigDecimal stdDev = stdDev(sorted, mean);
            result.add(upsert(parts[0], parts[1], mean, median(sorted), p90(sorted),
                    stdDev, sorted.size()));
        }
        audit.record("BASELINES_RECOMPUTED", "peer_baselines", String.valueOf(result.size()),
                "recomputed " + result.size() + " department/category baselines");
        log.info("Baseline recompute finished: {} buckets", result.size());
        return result;
    }

    /** Looks up the baseline for a department + category pair, if enough history exists. */
    public Optional<PeerBaseline> baselineFor(String department, String category) {
        return baselines.findByDepartmentAndCategory(department, category);
    }

    public List<PeerBaseline> all() {
        return baselines.findAll().stream()
                .sorted(Comparator.comparing(PeerBaseline::getDepartment)
                        .thenComparing(PeerBaseline::getCategory))
                .toList();
    }

    private PeerBaseline upsert(String department, String category, BigDecimal mean,
                                BigDecimal median, BigDecimal p90, BigDecimal stdDev, int count) {
        PeerBaseline baseline = baselines.findByDepartmentAndCategory(department, category)
                .orElse(new PeerBaseline(UUID.randomUUID().toString(), department, category,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        0, Instant.now(clock)));
        PeerBaseline updated = new PeerBaseline(baseline.getId(), department, category,
                scale(mean), scale(median), scale(p90), scale(stdDev), count, Instant.now(clock));
        return baselines.save(updated);
    }

    private static String key(ExpenseClaim claim) {
        return claim.getDepartment() + "|" + claim.getCategory();
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal mean(List<BigDecimal> sorted) {
        BigDecimal sum = sorted.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(sorted.size()), 6, RoundingMode.HALF_UP);
    }

    private static BigDecimal median(List<BigDecimal> sorted) {
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2);
        }
        return sorted.get(n / 2 - 1).add(sorted.get(n / 2))
                .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
    }

    private static BigDecimal p90(List<BigDecimal> sorted) {
        int index = Math.min(sorted.size() - 1, (int) Math.ceil(0.9 * sorted.size()) - 1);
        return sorted.get(Math.max(0, index));
    }

    private static BigDecimal stdDev(List<BigDecimal> sorted, BigDecimal meanValue) {
        BigDecimal sumSq = sorted.stream()
                .map(v -> v.subtract(meanValue).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double variance = sumSq.doubleValue() / Math.max(1, sorted.size());
        return BigDecimal.valueOf(Math.sqrt(variance)).setScale(6, RoundingMode.HALF_UP);
    }
}
