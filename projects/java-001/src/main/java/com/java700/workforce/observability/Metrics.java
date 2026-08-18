package com.java700.workforce.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Typed business metrics: ingest throughput, violations, evidence appends, exports. */
@Component
public class Metrics {

    private final Counter ingestedEvents;
    private final Counter violationsDetected;
    private final Counter evidenceAppends;
    private final Counter exportJobs;
    private final Timer exportDuration;
    private final Timer correlationDuration;
    private final AtomicLong openViolations = new AtomicLong();

    public Metrics(MeterRegistry registry) {
        this.ingestedEvents = Counter.builder("workforce.ingest.events")
                .description("Access events ingested").register(registry);
        this.violationsDetected = Counter.builder("workforce.violations.detected")
                .description("Compliance violations detected").register(registry);
        this.evidenceAppends = Counter.builder("workforce.evidence.appends")
                .description("Evidence ledger entries appended").register(registry);
        this.exportJobs = Counter.builder("workforce.export.jobs")
                .description("Audit export jobs created").register(registry);
        this.exportDuration = Timer.builder("workforce.export.duration")
                .description("Export build duration").register(registry);
        this.correlationDuration = Timer.builder("workforce.correlation.duration")
                .description("Correlation run duration").register(registry);
        registry.gauge("workforce.violations.open", openViolations);
    }

    public void incrementIngestedEvents() {
        ingestedEvents.increment();
    }

    public void incrementViolationsDetected() {
        violationsDetected.increment();
    }

    public void incrementEvidenceAppends() {
        evidenceAppends.increment();
    }

    public void incrementExportJobs() {
        exportJobs.increment();
    }

    public Timer exportDuration() {
        return exportDuration;
    }

    public Timer correlationDuration() {
        return correlationDuration;
    }

    public void setOpenViolations(long n) {
        openViolations.set(n);
    }
}
