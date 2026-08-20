package com.java700.legalmatter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

/** A computed court deadline on a matter. */
@Entity
@Table(name = "matter_deadlines")
public class MatterDeadline {

    public enum Status {
        OPEN, COMPLETED, MISSED, WAIVED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "matter_id", nullable = false, length = 36)
    private String matterId;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "jurisdiction", nullable = false, length = 32)
    private String jurisdiction;

    @Column(name = "trigger_date", nullable = false)
    private LocalDate triggerDate;

    @Column(name = "due_at", nullable = false)
    private LocalDate dueAt;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "completed_by", length = 120)
    private String completedBy;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected MatterDeadline() {
    }

    public MatterDeadline(String id, String matterId, String eventType, String jurisdiction,
                          LocalDate triggerDate, LocalDate dueAt) {
        this.id = id;
        this.matterId = matterId;
        this.eventType = eventType;
        this.jurisdiction = jurisdiction;
        this.triggerDate = triggerDate;
        this.dueAt = dueAt;
        this.status = Status.OPEN.name();
    }

    public String getId() {
        return id;
    }

    public String getMatterId() {
        return matterId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public LocalDate getDueAt() {
        return dueAt;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void complete(String newCompletedBy, Instant at) {
        this.status = Status.COMPLETED.name();
        this.completedBy = newCompletedBy;
        this.completedAt = at;
    }

    public void markMissed() {
        this.status = Status.MISSED.name();
    }
}
