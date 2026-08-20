package com.java700.achain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodic SLA escalation for stale approval requests. */
@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ApprovalScheduler {

    private static final Logger log = LoggerFactory.getLogger(ApprovalScheduler.class);

    private final ApprovalService service;

    public ApprovalScheduler(ApprovalService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "120000", initialDelayString = "45000")
    public void escalate() {
        try {
            Api.EscalationResult result = service.escalate();
            if (result.escalated() > 0) {
                log.info("Approval escalation: {} requests extended", result.escalated());
            }
        } catch (RuntimeException e) {
            log.error("Approval escalation failed", e);
        }
    }
}
