package com.java700.fleetmaint.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Append-only registry audit log: who did what to which record, when, with correlation id. */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "principal", length = 120)
    private String principal;

    @Column(name = "action", nullable = false, length = 64)
    private String action;

    @Column(name = "target_type", length = 64)
    private String targetType;

    @Column(name = "target_id", length = 64)
    private String targetId;

    @Column(name = "detail", length = 2000)
    private String detail;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    protected AuditLog() {
    }

    public AuditLog(String id, Instant occurredAt, String principal, String action,
                    String targetType, String targetId, String detail, String correlationId) {
        this.id = id;
        this.occurredAt = occurredAt;
        this.principal = principal;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.detail = detail;
        this.correlationId = correlationId;
    }
}
