package com.java700.workforce.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodic expiry sweep for grants past their expiry window. */
@Component
@ConditionalOnProperty(name = "app.sweep.enabled", havingValue = "true", matchIfMissing = true)
public class ExpirySweep {

    private static final Logger log = LoggerFactory.getLogger(ExpirySweep.class);

    private final AccessService service;

    public ExpirySweep(AccessService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${app.sweep.fixed-delay-ms:60000}", initialDelayString = "30000")
    public void sweep() {
        int expired = service.expireDueGrants();
        if (expired > 0) {
            log.info("Grant expiry sweep: {} grants expired", expired);
        }
    }
}
