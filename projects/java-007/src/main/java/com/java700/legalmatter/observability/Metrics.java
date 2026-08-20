package com.java700.legalmatter.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Typed business metrics for legal-matter operations. */
@Component
public class Metrics {

    private final Counter screensRun;
    private final Counter conflictsDetected;
    private final Counter deadlinesMissed;
    private final AtomicLong openDeadlines = new AtomicLong();

    public Metrics(MeterRegistry registry) {
        this.screensRun = Counter.builder("legal.screens.run")
                .description("Conflict screens run").register(registry);
        this.conflictsDetected = Counter.builder("legal.conflicts.detected")
                .description("Conflicts detected").register(registry);
        this.deadlinesMissed = Counter.builder("legal.deadlines.missed")
                .description("Deadlines missed").register(registry);
        registry.gauge("legal.deadlines.open", openDeadlines);
    }

    public void incrementScreensRun() {
        screensRun.increment();
    }

    public void incrementConflictsDetected() {
        conflictsDetected.increment();
    }

    public void incrementDeadlinesMissed() {
        deadlinesMissed.increment();
    }

    public void setOpenDeadlines(long n) {
        openDeadlines.set(n);
    }
}
