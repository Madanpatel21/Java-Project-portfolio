package com.java700.roster.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/** Typed business metrics for the rostering optimizer. */
@Component
public class Metrics {

    private final Counter rostersCreated;
    private final Counter rostersOptimized;
    private final Counter rostersPublished;
    private final Counter swapsApproved;
    private final Counter swapsRejected;
    private final Timer optimizationDuration;

    public Metrics(MeterRegistry registry) {
        this.rostersCreated = registry.counter("roster.rosters.created");
        this.rostersOptimized = registry.counter("roster.rosters.optimized");
        this.rostersPublished = registry.counter("roster.rosters.published");
        this.swapsApproved = registry.counter("roster.swaps.approved");
        this.swapsRejected = registry.counter("roster.swaps.rejected");
        this.optimizationDuration = registry.timer("roster.optimization.duration");
    }

    public void rosterCreated() {
        rostersCreated.increment();
    }

    public void rosterOptimized() {
        rostersOptimized.increment();
    }

    public void rosterPublished() {
        rostersPublished.increment();
    }

    public void swapApproved() {
        swapsApproved.increment();
    }

    public void swapRejected() {
        swapsRejected.increment();
    }

    public Timer optimizationDuration() {
        return optimizationDuration;
    }
}
