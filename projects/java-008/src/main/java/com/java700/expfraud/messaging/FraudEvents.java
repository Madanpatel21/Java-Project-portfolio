package com.java700.expfraud.messaging;

import java.time.Instant;

/** Domain events published on the expense event bus. */
public final class FraudEvents {

    private FraudEvents() {
    }

    public record ClaimSubmitted(String eventId, Instant occurredAt, String claimId, String claimNo) implements DomainEvent {
    }

    public record ClaimScored(String eventId, Instant occurredAt, String claimId, String claimNo,
                              int riskScore, boolean autoCase) implements DomainEvent {
    }

    public record CaseOpened(String eventId, Instant occurredAt, String caseId, String caseNo,
                             String claimId, int riskScore) implements DomainEvent {
    }

    public record CaseDecided(String eventId, Instant occurredAt, String caseId, String caseNo,
                              String decision, String decidedBy) implements DomainEvent {
    }
}
