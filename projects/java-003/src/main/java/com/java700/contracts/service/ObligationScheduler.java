package com.java700.contracts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Daily SLA scan: reminders inside the window, overdue detection past due date. */
@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ObligationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ObligationScheduler.class);

    private final ContractService service;

    public ObligationScheduler(ContractService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "300000", initialDelayString = "45000")
    public void scan() {
        try {
            Api.ScanResult result = service.scan();
            if (result.notified() + result.overdue() > 0) {
                log.info("Scheduled obligation scan: {} notified, {} overdue",
                        result.notified(), result.overdue());
            }
        } catch (RuntimeException e) {
            log.error("Scheduled obligation scan failed", e);
        }
    }
}
