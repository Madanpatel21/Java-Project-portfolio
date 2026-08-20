package com.java700.govault.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Typed business metrics for document governance. */
@Component
public class Metrics {

    private final Counter documentsUploaded;
    private final Counter documentsClassified;
    private final Counter documentsDisposed;
    private final Counter holdsApplied;
    private final AtomicLong quarantined = new AtomicLong();

    public Metrics(MeterRegistry registry) {
        this.documentsUploaded = Counter.builder("govault.documents.uploaded")
                .description("Documents uploaded").register(registry);
        this.documentsClassified = Counter.builder("govault.documents.classified")
                .description("Documents classified").register(registry);
        this.documentsDisposed = Counter.builder("govault.documents.disposed")
                .description("Documents disposed").register(registry);
        this.holdsApplied = Counter.builder("govault.holds.applied")
                .description("Legal holds applied").register(registry);
        registry.gauge("govault.documents.quarantined", quarantined);
    }

    public void incrementUploaded() {
        documentsUploaded.increment();
    }

    public void incrementClassified() {
        documentsClassified.increment();
    }

    public void incrementDisposed() {
        documentsDisposed.increment();
    }

    public void incrementHoldsApplied() {
        holdsApplied.increment();
    }

    public void setQuarantined(long n) {
        quarantined.set(n);
    }
}
