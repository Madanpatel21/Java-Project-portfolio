package com.java700.p2p.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Typed business metrics for the reconciliation pipeline. */
@Component
public class Metrics {

    private final Counter invoicesIngested;
    private final Counter invoicesMatched;
    private final Counter exceptionsCreated;
    private final Counter exceptionsWaived;
    private final Counter postingsCreated;
    private final Timer matchDuration;
    private final AtomicLong openExceptions = new AtomicLong();

    public Metrics(MeterRegistry registry) {
        this.invoicesIngested = Counter.builder("p2p.invoices.ingested")
                .description("Invoices ingested").register(registry);
        this.invoicesMatched = Counter.builder("p2p.invoices.matched")
                .description("Invoices auto-matched").register(registry);
        this.exceptionsCreated = Counter.builder("p2p.exceptions.created")
                .description("Match exceptions created").register(registry);
        this.exceptionsWaived = Counter.builder("p2p.exceptions.waived")
                .description("Exceptions waived by manager").register(registry);
        this.postingsCreated = Counter.builder("p2p.postings.created")
                .description("GL postings created").register(registry);
        this.matchDuration = Timer.builder("p2p.match.duration")
                .description("Three-way match duration").register(registry);
        registry.gauge("p2p.exceptions.open", openExceptions);
    }

    public void incrementInvoicesIngested() {
        invoicesIngested.increment();
    }

    public void incrementInvoicesMatched() {
        invoicesMatched.increment();
    }

    public void incrementExceptionsCreated() {
        exceptionsCreated.increment();
    }

    public void incrementExceptionsWaived() {
        exceptionsWaived.increment();
    }

    public void incrementPostingsCreated() {
        postingsCreated.increment();
    }

    public Timer matchDuration() {
        return matchDuration;
    }

    public void setOpenExceptions(long n) {
        openExceptions.set(n);
    }
}
