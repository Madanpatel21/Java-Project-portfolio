package com.java700.expfraud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** An explainable policy violation recorded against a claim during scoring. */
@Entity
@Table(name = "rule_violations")
public class RuleViolation {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "claim_id", length = 36, nullable = false)
    private String claimId;

    @Column(name = "rule_code", length = 40, nullable = false)
    private String ruleCode;

    @Column(name = "rule_message", length = 500, nullable = false)
    private String ruleMessage;

    @Column(length = 200)
    private String observed;

    @Column(length = 200)
    private String expected;

    @Column(length = 16, nullable = false)
    private String severity;

    @Column(nullable = false)
    private int points;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RuleViolation() {
    }

    public RuleViolation(String id, String claimId, String ruleCode, String ruleMessage,
                         String observed, String expected, String severity, int points,
                         Instant createdAt) {
        this.id = id;
        this.claimId = claimId;
        this.ruleCode = ruleCode;
        this.ruleMessage = ruleMessage;
        this.observed = observed;
        this.expected = expected;
        this.severity = severity;
        this.points = points;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getClaimId() {
        return claimId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public String getRuleMessage() {
        return ruleMessage;
    }

    public String getObserved() {
        return observed;
    }

    public String getExpected() {
        return expected;
    }

    public String getSeverity() {
        return severity;
    }

    public int getPoints() {
        return points;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
