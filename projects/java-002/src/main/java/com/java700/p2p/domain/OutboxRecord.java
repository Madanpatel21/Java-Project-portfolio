package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Transactional outbox record for downstream integration events. */
@Entity
@Table(name = "outbox")
public class OutboxRecord {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "payload", nullable = false, length = 8192)
    private String payload;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OutboxRecord() {
    }

    public OutboxRecord(String id, String eventType, String payload, Instant createdAt) {
        this.id = id;
        this.eventType = eventType;
        this.payload = payload;
        this.status = "PENDING";
        this.createdAt = createdAt;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markSent() {
        this.status = "SENT";
    }
}
