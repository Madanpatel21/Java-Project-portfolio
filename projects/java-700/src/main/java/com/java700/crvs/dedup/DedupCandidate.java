package com.java700.crvs.dedup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** Potential duplicate identity detected by the fuzzy matching engine. */
@Entity
@Table(name = "dedup_candidates")
public class DedupCandidate {

    public enum Status {
        OPEN, CONFIRMED, DISMISSED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "person_a_id", nullable = false, length = 36)
    private String personAId;

    @Column(name = "person_b_id", nullable = false, length = 36)
    private String personBId;

    @Column(name = "score", nullable = false, precision = 5, scale = 4)
    private BigDecimal score;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "decided_by", length = 120)
    private String decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    protected DedupCandidate() {
    }

    public DedupCandidate(String id, String personAId, String personBId, BigDecimal score,
                          Instant createdAt) {
        this.id = id;
        this.personAId = personAId;
        this.personBId = personBId;
        this.score = score;
        this.status = Status.OPEN.name();
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getPersonAId() {
        return personAId;
    }

    public String getPersonBId() {
        return personBId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    void decide(Status newStatus, String newDecidedBy, Instant newDecidedAt) {
        this.status = newStatus.name();
        this.decidedBy = newDecidedBy;
        this.decidedAt = newDecidedAt;
    }
}
