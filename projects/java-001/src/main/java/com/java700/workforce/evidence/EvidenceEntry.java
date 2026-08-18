package com.java700.workforce.evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One link of the append-only evidence ledger. Written in the same transaction as the
 * domain mutation it evidences; never updated or deleted by application code.
 */
@Entity
@Table(name = "evidence_entry")
public class EvidenceEntry {

    @Id
    @Column(name = "seq")
    private Long seq;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "actor", nullable = false, length = 120)
    private String actor;

    @Column(name = "payload", nullable = false, length = 8192)
    private String payload;

    @Column(name = "prev_hash", nullable = false, length = 64)
    private String prevHash;

    @Column(name = "hash", nullable = false, length = 64)
    private String hash;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected EvidenceEntry() {
    }

    public EvidenceEntry(Long seq, String aggregateType, String aggregateId, String eventType,
                         String actor, String payload, String prevHash, String hash, Instant occurredAt) {
        this.seq = seq;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.actor = actor;
        this.payload = payload;
        this.prevHash = prevHash;
        this.hash = hash;
        this.occurredAt = occurredAt;
    }

    public Long getSeq() {
        return seq;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getActor() {
        return actor;
    }

    public String getPayload() {
        return payload;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public String getHash() {
        return hash;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
