package com.java700.stewardship.restricted;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Time-boxed ID-physician approval for a restricted antimicrobial. */
@Entity
@Table(name = "restricted_authorizations")
public class RestrictedAuthorization {

    public enum Status {
        PENDING, APPROVED, REJECTED, EXPIRED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "prescription_id", nullable = false, length = 36)
    private String prescriptionId;

    @Column(name = "requested_by", nullable = false, length = 120)
    private String requestedBy;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "approved_by", length = 120)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "status", nullable = false, length = 24)
    private String status;

    @Column(name = "reason", length = 1000)
    private String reason;

    protected RestrictedAuthorization() {
    }

    public RestrictedAuthorization(String id, String prescriptionId, String requestedBy,
                                   Instant requestedAt, String reason) {
        this.id = id;
        this.prescriptionId = prescriptionId;
        this.requestedBy = requestedBy;
        this.requestedAt = requestedAt;
        this.reason = reason;
        this.status = Status.PENDING.name();
    }

    public String getId() {
        return id;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public String getReason() {
        return reason;
    }

    void approve(String approver, Instant newApprovedAt, Instant newExpiresAt) {
        this.status = Status.APPROVED.name();
        this.approvedBy = approver;
        this.approvedAt = newApprovedAt;
        this.expiresAt = newExpiresAt;
    }

    void reject(String approver, Instant at, String note) {
        this.status = Status.REJECTED.name();
        this.approvedBy = approver;
        this.approvedAt = at;
        this.reason = note;
    }

    void expire(Instant at) {
        this.status = Status.EXPIRED.name();
        this.expiresAt = at;
    }
}
