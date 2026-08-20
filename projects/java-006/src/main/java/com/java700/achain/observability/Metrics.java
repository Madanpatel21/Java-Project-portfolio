package com.java700.achain.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Typed business metrics for the approval engine. */
@Component
public class Metrics {

    private final Counter requestsCreated;
    private final Counter requestsApproved;
    private final Counter decisionsRecorded;
    private final AtomicLong pendingRequests = new AtomicLong();

    public Metrics(MeterRegistry registry) {
        this.requestsCreated = Counter.builder("achain.requests.created")
                .description("Approval requests created").register(registry);
        this.requestsApproved = Counter.builder("achain.requests.approved")
                .description("Approval requests approved").register(registry);
        this.decisionsRecorded = Counter.builder("achain.decisions.recorded")
                .description("Approval decisions recorded").register(registry);
        registry.gauge("achain.requests.pending", pendingRequests);
    }

    public void incrementRequestsCreated() {
        requestsCreated.increment();
    }

    public void incrementRequestsApproved() {
        requestsApproved.increment();
    }

    public void incrementDecisionsRecorded() {
        decisionsRecorded.increment();
    }

    public void setPendingRequests(long n) {
        pendingRequests.set(n);
    }
}
