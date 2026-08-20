package com.java700.legalmatter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A court-calendar rule: event type + jurisdiction → offset from the trigger date. */
@Entity
@Table(name = "deadline_rules")
public class DeadlineRule {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "jurisdiction", nullable = false, length = 32)
    private String jurisdiction;

    @Column(name = "days_offset", nullable = false)
    private int daysOffset;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected DeadlineRule() {
    }

    public DeadlineRule(String id, String eventType, String jurisdiction, int daysOffset) {
        this.id = id;
        this.eventType = eventType;
        this.jurisdiction = jurisdiction;
        this.daysOffset = daysOffset;
        this.active = true;
    }

    public String getEventType() {
        return eventType;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public int getDaysOffset() {
        return daysOffset;
    }

    public boolean isActive() {
        return active;
    }
}
