package com.java700.workforce.compliance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Cron trigger for the correlation engine (disabled in tests via app.correlation.enabled=false). */
@Component
@ConditionalOnProperty(name = "app.correlation.enabled", havingValue = "true", matchIfMissing = true)
public class CorrelationScheduler {

    private static final Logger log = LoggerFactory.getLogger(CorrelationScheduler.class);

    private final CorrelationJob job;

    public CorrelationScheduler(CorrelationJob job) {
        this.job = job;
    }

    @Scheduled(cron = "${app.correlation.cron:0 */5 * * * *}")
    public void scheduledRun() {
        try {
            job.run();
        } catch (RuntimeException e) {
            log.error("Scheduled correlation run failed", e);
        }
    }
}
