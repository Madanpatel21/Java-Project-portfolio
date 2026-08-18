package com.java700.workforce.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Raw access activity (login/use events) ingested from systems and monitored for
 * account-inactivity rules. High volume — stored separately from the evidence ledger.
 */
@Entity
@Table(name = "access_event")
public class AccessEvent {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "resource_name", nullable = false, length = 120)
    private String resourceName;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "source", nullable = false, length = 64)
    private String source;

    @Column(name = "external_id", length = 128)
    private String externalId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AccessEvent() {
    }

    public AccessEvent(String id, String userId, String resourceName, String eventType,
                       String ipAddress, String source, String externalId, Instant occurredAt) {
        this.id = id;
        this.userId = userId;
        this.resourceName = resourceName;
        this.eventType = eventType;
        this.ipAddress = ipAddress;
        this.source = source;
        this.externalId = externalId;
        this.occurredAt = occurredAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
