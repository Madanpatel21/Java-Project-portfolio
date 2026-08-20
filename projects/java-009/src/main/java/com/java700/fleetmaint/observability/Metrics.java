package com.java700.fleetmaint.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/** Typed business metrics for the fleet maintenance platform. */
@Component
public class Metrics {

    private final Counter tasksForecasted;
    private final Counter tasksOverdue;
    private final Counter workOrdersCompleted;
    private final Counter workOrdersOnPartsHold;
    private final Counter partsIssued;
    private final Counter tamperFlags;
    private final Counter inspectionsFailed;

    public Metrics(MeterRegistry registry) {
        this.tasksForecasted = registry.counter("fleet.tasks.forecasted");
        this.tasksOverdue = registry.counter("fleet.tasks.overdue");
        this.workOrdersCompleted = registry.counter("fleet.workorders.completed");
        this.workOrdersOnPartsHold = registry.counter("fleet.workorders.parts_hold");
        this.partsIssued = registry.counter("fleet.parts.issued");
        this.tamperFlags = registry.counter("fleet.odometer.tamper_flags");
        this.inspectionsFailed = registry.counter("fleet.inspections.failed");
    }

    public void taskForecasted() {
        tasksForecasted.increment();
    }

    public void taskOverdue() {
        tasksOverdue.increment();
    }

    public void workOrderCompleted() {
        workOrdersCompleted.increment();
    }

    public void workOrderOnPartsHold() {
        workOrdersOnPartsHold.increment();
    }

    public void partsIssued(int quantity) {
        partsIssued.increment(quantity);
    }

    public void tamperFlag() {
        tamperFlags.increment();
    }

    public void inspectionFailed() {
        inspectionsFailed.increment();
    }
}
