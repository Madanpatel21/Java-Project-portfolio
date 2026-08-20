package com.java700.kit.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Typed business metrics for the registry. */
@Component
public class Metrics {

    private final Counter ledgerAppends;
    private final Counter registrationsApproved;
    private final Counter certificatesIssued;
    private final Counter dedupCandidates;
    private final AtomicLong openRegistrations = new AtomicLong();

    public Metrics(MeterRegistry registry) {
        this.ledgerAppends = Counter.builder("crvs.ledger.appends")
                .description("Life-event ledger entries appended").register(registry);
        this.registrationsApproved = Counter.builder("crvs.registrations.approved")
                .description("Registrations approved").register(registry);
        this.certificatesIssued = Counter.builder("crvs.certificates.issued")
                .description("Certificates issued").register(registry);
        this.dedupCandidates = Counter.builder("crvs.dedup.candidates")
                .description("Duplicate candidates detected").register(registry);
        registry.gauge("crvs.registrations.open", openRegistrations);
    }

    public void incrementLedgerAppends() {
        ledgerAppends.increment();
    }

    public void incrementRegistrationsApproved() {
        registrationsApproved.increment();
    }

    public void incrementCertificatesIssued() {
        certificatesIssued.increment();
    }

    public void incrementDedupCandidates() {
        dedupCandidates.increment();
    }

    public void setOpenRegistrations(long n) {
        openRegistrations.set(n);
    }
}
