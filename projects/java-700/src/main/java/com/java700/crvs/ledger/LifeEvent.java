package com.java700.crvs.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One link of the dual-chained life-event ledger: the GLOBAL chain links every event in the
 * registry (whole-registry tamper evidence); the per-person CHAIN links one person's life
 * history (individual integrity). Both hashes are computed over the previous link of their
 * own chain plus the canonical event payload.
 */
@Entity
@Table(name = "life_events")
public class LifeEvent {

    @Id
    @Column(name = "global_seq")
    private Long globalSeq;

    @Column(name = "person_id", nullable = false, length = 36)
    private String personId;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "payload", nullable = false, length = 8192)
    private String payload;

    @Column(name = "actor", nullable = false, length = 120)
    private String actor;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "prev_global_hash", nullable = false, length = 64)
    private String prevGlobalHash;

    @Column(name = "global_hash", nullable = false, length = 64)
    private String globalHash;

    @Column(name = "chain_seq", nullable = false)
    private Long chainSeq;

    @Column(name = "prev_chain_hash", nullable = false, length = 64)
    private String prevChainHash;

    @Column(name = "chain_hash", nullable = false, length = 64)
    private String chainHash;

    protected LifeEvent() {
    }

    public LifeEvent(Long globalSeq, String personId, String eventType, String payload,
                     String actor, Instant occurredAt, String prevGlobalHash, String globalHash,
                     Long chainSeq, String prevChainHash, String chainHash) {
        this.globalSeq = globalSeq;
        this.personId = personId;
        this.eventType = eventType;
        this.payload = payload;
        this.actor = actor;
        this.occurredAt = occurredAt;
        this.prevGlobalHash = prevGlobalHash;
        this.globalHash = globalHash;
        this.chainSeq = chainSeq;
        this.prevChainHash = prevChainHash;
        this.chainHash = chainHash;
    }

    public Long getGlobalSeq() {
        return globalSeq;
    }

    public String getPersonId() {
        return personId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getActor() {
        return actor;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getPrevGlobalHash() {
        return prevGlobalHash;
    }

    public String getGlobalHash() {
        return globalHash;
    }

    public Long getChainSeq() {
        return chainSeq;
    }

    public String getPrevChainHash() {
        return prevChainHash;
    }

    public String getChainHash() {
        return chainHash;
    }
}
