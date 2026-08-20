package com.java700.legalmatter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Daily scan for missed court deadlines. */
@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class DeadlineScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeadlineScheduler.class);

    private final LegalService service;

    public DeadlineScheduler(LegalService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "180000", initialDelayString = "45000")
    public void scan() {
        try {
            int missed = service.markMissed();
            if (missed > 0) {
                log.info("Deadline scan: {} deadlines missed", missed);
            }
        } catch (RuntimeException e) {
            log.error("Deadline scan failed", e);
        }
    }
}
