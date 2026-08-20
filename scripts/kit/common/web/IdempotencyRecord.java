package com.java700.kit.common.web;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "idempotency_record")
public class IdempotencyRecord {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "idem_key", nullable = false, unique = true, length = 200)
    private String key;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "resource_id", length = 64)
    private String resourceId;

    @Column(name = "response_status", nullable = false)
    private int responseStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyRecord() {
    }

    IdempotencyRecord(String id, String key, String resourceType, String resourceId,
                      int responseStatus, Instant createdAt) {
        this.id = id;
        this.key = key;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.responseStatus = responseStatus;
        this.createdAt = createdAt;
    }

    String getResourceType() {
        return resourceType;
    }

    String getId() {
        return id;
    }

    String getResourceId() {
        return resourceId;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
