package com.java700.stewardship.reviews;

import com.java700.stewardship.guidelines.GuidelineRepository;
import com.java700.stewardship.guidelines.StewardshipRuleEngine;
import com.java700.stewardship.prescriptions.Prescription;
import com.java700.stewardship.prescriptions.PrescriptionRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Scheduled stewardship scans: time-based review triggers. */
@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class StewardshipSchedulers {

    private static final Logger log = LoggerFactory.getLogger(StewardshipSchedulers.class);

    private final PrescriptionRepository prescriptionRepository;
    private final GuidelineRepository guidelineRepository;
    private final StewardshipRuleEngine ruleEngine;
    private final ReviewTaskService reviewTasks;
    private final Clock clock;

    public StewardshipSchedulers(PrescriptionRepository prescriptionRepository,
                                 GuidelineRepository guidelineRepository,
                                 StewardshipRuleEngine ruleEngine, ReviewTaskService reviewTasks,
                                 Clock clock) {
        this.prescriptionRepository = prescriptionRepository;
        this.guidelineRepository = guidelineRepository;
        this.ruleEngine = ruleEngine;
        this.reviewTasks = reviewTasks;
        this.clock = clock;
    }

    /** Scans ACTIVE empiric prescriptions and opens TIME_BASED review tasks. */
    @Scheduled(initialDelayString = "${app.scheduler.review-scan-delay-ms:30000}",
               fixedDelayString = "${app.scheduler.review-scan-interval-ms:300000}")
    public void reviewScan() {
        int created = 0;
        var reviewRule = guidelineRepository.findByStatus("ACTIVE")
                .map(g -> ruleEngine.reviewTriggerRule(g.getRulesJson())).orElse(null);
        if (reviewRule == null) {
            return;
        }
        for (Prescription rx : prescriptionRepository.findByStatus("ACTIVE")) {
            if (rx.isEmpiric() && ruleEngine.reviewDue(rx, reviewRule, Instant.now(clock))) {
                reviewTasks.createIfAbsent(rx.getId(), "TIME_BASED", Instant.now(clock));
                created++;
            }
        }
        if (created > 0) {
            log.info("Review scan: {} time-based review tasks created", created);
        }
    }
}
