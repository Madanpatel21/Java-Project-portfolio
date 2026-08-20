package com.java700.contracts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Append-only history of an obligation's lifecycle events. */
@Entity
@Table(name = "obligation_events")
public class ObligationEvent {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "obligation_id", nullable = false, length = 36)
    private String obligationId;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "detail", length = 1000)
    private String detail;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected ObligationEvent() {
    }

    public ObligationEvent(String id, String obligationId, String eventType, String detail,
                           Instant occurredAt) {
        this.id = id;
        this.obligationId = obligationId;
        this.eventType = eventType;
        this.detail = detail;
        this.occurredAt = occurredAt;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDetail() {
        return detail;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
