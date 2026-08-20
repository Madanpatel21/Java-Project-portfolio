package com.java700.govault.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Daily retention scan: dispose past-retention documents not protected by holds. */
@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class RetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetentionScheduler.class);

    private final DocumentService service;

    public RetentionScheduler(DocumentService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "300000", initialDelayString = "45000")
    public void scan() {
        try {
            Api.ScanResult result = service.scan();
            if (result.disposed() + result.protectedByHold() > 0) {
                log.info("Retention scan: {} disposed, {} protected by holds",
                        result.disposed(), result.protectedByHold());
            }
        } catch (RuntimeException e) {
            log.error("Retention scan failed", e);
        }
    }
}
