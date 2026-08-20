package com.java700.expfraud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Nightly peer-baseline recompute so outlier detection always tracks the latest
 * legitimate spending patterns. Disabled in tests and in profiles that opt out.
 */
@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BaselineScheduler {

    private static final Logger log = LoggerFactory.getLogger(BaselineScheduler.class);

    private final BaselineService baselines;

    public BaselineScheduler(BaselineService baselines) {
        this.baselines = baselines;
    }

    @Scheduled(cron = "${app.scheduler.baseline-cron:0 15 2 * * *}")
    public void recomputeBaselines() {
        log.info("Scheduled peer-baseline recompute starting");
        int buckets = baselines.recomputeAll().size();
        log.info("Scheduled peer-baseline recompute finished: {} buckets", buckets);
    }
}
