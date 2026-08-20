package com.java700.govault.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Retention schedule per class (R0..R7). -1 days = permanent. */
@Entity
@Table(name = "retention_rules")
public class RetentionRule {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "retention_class", nullable = false, unique = true, length = 8)
    private String retentionClass;

    @Column(name = "retention_days", nullable = false)
    private int retentionDays;

    @Column(name = "action", nullable = false, length = 16)
    private String action;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected RetentionRule() {
    }

    public String getRetentionClass() {
        return retentionClass;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public String getAction() {
        return action;
    }

    public boolean isActive() {
        return active;
    }
}
