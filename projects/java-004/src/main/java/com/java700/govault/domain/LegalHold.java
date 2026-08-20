package com.java700.govault.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A litigation/preservation hold that blocks disposition of its documents. */
@Entity
@Table(name = "legal_holds")
public class LegalHold {

    public enum Status {
        ACTIVE, RELEASED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "applied_by", nullable = false, length = 120)
    private String appliedBy;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    protected LegalHold() {
    }

    public LegalHold(String id, String name, String reason, String appliedBy, Instant appliedAt) {
        this.id = id;
        this.name = name;
        this.reason = reason;
        this.appliedBy = appliedBy;
        this.appliedAt = appliedAt;
        this.status = Status.ACTIVE.name();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getReason() {
        return reason;
    }

    public String getAppliedBy() {
        return appliedBy;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public void release(Instant at) {
        this.status = Status.RELEASED.name();
        this.releasedAt = at;
    }
}
