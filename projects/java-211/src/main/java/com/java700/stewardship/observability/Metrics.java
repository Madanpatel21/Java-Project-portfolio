package com.java700.stewardship.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Typed business metrics: prescriptions, interventions, alerts, evaluations. */
@Component
public class Metrics {

    private final Counter prescriptionsCreated;
    private final Counter interventionsProposed;
    private final Counter interventionsAccepted;
    private final Counter drugBugMismatchAlerts;
    private final Counter reviewTasksCreated;
    private final Timer evaluationDuration;
    private final AtomicLong openReviewTasks = new AtomicLong();

    public Metrics(MeterRegistry registry) {
        this.prescriptionsCreated = Counter.builder("stewardship.prescriptions.created")
                .description("Prescriptions ordered").register(registry);
        this.interventionsProposed = Counter.builder("stewardship.interventions.proposed")
                .description("Interventions proposed").register(registry);
        this.interventionsAccepted = Counter.builder("stewardship.interventions.accepted")
                .description("Interventions accepted").register(registry);
        this.drugBugMismatchAlerts = Counter.builder("stewardship.alerts.drug_bug_mismatch")
                .description("Drug-bug mismatch alerts").register(registry);
        this.reviewTasksCreated = Counter.builder("stewardship.reviews.created")
                .description("Review tasks created").register(registry);
        this.evaluationDuration = Timer.builder("stewardship.evaluation.duration")
                .description("Rule-engine evaluation duration").register(registry);
        registry.gauge("stewardship.reviews.open", openReviewTasks);
    }

    public void incrementPrescriptions() {
        prescriptionsCreated.increment();
    }

    public void incrementInterventionsProposed() {
        interventionsProposed.increment();
    }

    public void incrementInterventionsAccepted() {
        interventionsAccepted.increment();
    }

    public void incrementDrugBugMismatchAlerts() {
        drugBugMismatchAlerts.increment();
    }

    public void incrementReviewTasks() {
        reviewTasksCreated.increment();
    }

    public Timer evaluationDuration() {
        return evaluationDuration;
    }

    public void setOpenReviewTasks(long n) {
        openReviewTasks.set(n);
    }
}
