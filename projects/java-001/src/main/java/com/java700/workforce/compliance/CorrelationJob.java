package com.java700.workforce.compliance;



import com.java700.workforce.access.GrantRepository;
import com.java700.workforce.events.AccessEventRepository;
import com.java700.workforce.identity.UserProfile;
import com.java700.workforce.identity.UserProfileRepository;
import com.java700.workforce.observability.Metrics;
import com.java700.workforce.policy.PolicyService;
import com.java700.workforce.policy.RuleEngine;
import com.java700.workforce.policy.RuleEvaluator;
import com.java700.workforce.policy.ViolationCandidate;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The compliance correlation engine: assembles a snapshot of the workforce state
 * (active grants, users, latest activity), evaluates every active policy rule,
 * and materializes violations. Idempotent: reruns dedupe against open violations.
 */
@Component
public class CorrelationJob {

    private static final Logger log = LoggerFactory.getLogger(CorrelationJob.class);
    public static final String POLICY_CODE = "ACCESS_GOVERNANCE";

    private final GrantRepository grantRepository;
    private final UserProfileRepository userRepository;
    private final AccessEventRepository eventRepository;
    private final PolicyService policyService;
    private final RuleEngine ruleEngine;
    private final ViolationService violationService;
    private final Metrics metrics;
    private final Clock clock;

    public CorrelationJob(GrantRepository grantRepository, UserProfileRepository userRepository,
                          AccessEventRepository eventRepository, PolicyService policyService,
                          RuleEngine ruleEngine, ViolationService violationService,
                          Metrics metrics, Clock clock) {
        this.grantRepository = grantRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.policyService = policyService;
        this.ruleEngine = ruleEngine;
        this.violationService = violationService;
        this.metrics = metrics;
        this.clock = clock;
    }

    public ComplianceApi.RunResult run() {
        long startedAt = System.nanoTime();
        try {
            Instant now = Instant.now(clock);
            var ctx = new RuleEvaluator.EvaluationContext(now,
                    grantRepository.findByStatus("ACTIVE"),
                    userRepository.findByStatus("ACTIVE").stream()
                            .collect(Collectors.toMap(UserProfile::getId, Function.identity())),
                    latestEventByUser());
            int created = 0;
            for (RuleEngine.RuleDefinition rule : ruleEngine.parse(POLICY_CODE, policyService.activeRules(POLICY_CODE))) {
                List<ViolationCandidate> hits = ruleEngine.evaluatorFor(rule.type()).evaluate(rule, ctx);
                for (ViolationCandidate hit : hits) {
                    if (violationService.detect(hit) != null) {
                        created++;
                    }
                }
            }
            metrics.setOpenViolations(violationService.openCount());
            log.info("Correlation run finished: {} new violations, {} open total",
                    created, violationService.openCount());
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            return new ComplianceApi.RunResult(created, 0, durationMs);
        } finally {
            long elapsed = System.nanoTime() - startedAt;
            metrics.correlationDuration().record(elapsed, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    private Map<String, Instant> latestEventByUser() {
        Map<String, Instant> out = new HashMap<>();
        for (var row : eventRepository.findLatestByUser()) {
            if (row.getUserId() != null && row.getLast() != null) {
                out.put(row.getUserId(), row.getLast());
            }
        }
        return out;
    }
}
