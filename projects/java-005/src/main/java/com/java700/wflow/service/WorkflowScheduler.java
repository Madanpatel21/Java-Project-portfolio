package com.java700.wflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodic timer resume + task escalation. */
@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class WorkflowScheduler {

    private static final Logger log = LoggerFactory.getLogger(WorkflowScheduler.class);

    private final WorkflowService service;

    public WorkflowScheduler(WorkflowService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "60000", initialDelayString = "30000")
    public void tick() {
        try {
            Api.TimerResult timers = service.resumeTimers();
            Api.EscalationResult escalations = service.escalateOverdue();
            if (timers.resumed() + escalations.escalated() > 0) {
                log.info("Workflow scheduler: {} resumed, {} escalated",
                        timers.resumed(), escalations.escalated());
            }
        } catch (RuntimeException e) {
            log.error("Workflow scheduler tick failed", e);
        }
    }
}
