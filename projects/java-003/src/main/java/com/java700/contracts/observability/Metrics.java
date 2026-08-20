package com.java700.contracts.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Typed business metrics for the contract/obligation platform. */
@Component
public class Metrics {

    private final Counter contractsCreated;
    private final Counter obligationsCreated;
    private final Counter obligationsCompleted;
    private final Counter obligationsWaived;
    private final Counter remindersSent;
    private final AtomicLong openObligations = new AtomicLong();

    public Metrics(MeterRegistry registry) {
        this.contractsCreated = Counter.builder("contracts.created")
                .description("Contracts created").register(registry);
        this.obligationsCreated = Counter.builder("obligations.created")
                .description("Obligations created").register(registry);
        this.obligationsCompleted = Counter.builder("obligations.completed")
                .description("Obligations completed").register(registry);
        this.obligationsWaived = Counter.builder("obligations.waived")
                .description("Obligations waived").register(registry);
        this.remindersSent = Counter.builder("obligations.reminders.sent")
                .description("Reminder notifications sent").register(registry);
        registry.gauge("obligations.open", openObligations);
    }

    public void incrementContractsCreated() {
        contractsCreated.increment();
    }

    public void incrementObligationsCreated() {
        obligationsCreated.increment();
    }

    public void incrementObligationsCompleted() {
        obligationsCompleted.increment();
    }

    public void incrementObligationsWaived() {
        obligationsWaived.increment();
    }

    public void incrementRemindersSent() {
        remindersSent.increment();
    }

    public void setOpenObligations(long n) {
        openObligations.set(n);
    }
}
