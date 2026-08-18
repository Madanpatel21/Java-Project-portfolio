package com.java700.workforce.compliance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Detected policy violation with a controlled lifecycle: OPEN → ACKNOWLEDGED → REMEDIATED → CLOSED. */
@Entity
@Table(name = "violation")
public class Violation {

    public enum Status {
        OPEN, ACKNOWLEDGED, REMEDIATED, CLOSED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "policy_code", nullable = false, length = 64)
    private String policyCode;

    @Column(name = "rule_type", nullable = false, length = 64)
    private String ruleType;

    @Column(name = "severity", nullable = false, length = 16)
    private String severity;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "evidence_seq")
    private Long evidenceSeq;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "remediated_at")
    private Instant remediatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "remediation_note", length = 2000)
    private String remediationNote;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    protected Violation() {
    }

    public Violation(String id, String userId, String policyCode, String ruleType, String severity,
                     String description, Instant detectedAt) {
        this.id = id;
        this.userId = userId;
        this.policyCode = policyCode;
        this.ruleType = ruleType;
        this.severity = severity;
        this.description = description;
        this.status = Status.OPEN.name();
        this.detectedAt = detectedAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public String getRuleType() {
        return ruleType;
    }

    public String getSeverity() {
        return severity;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public String getDescription() {
        return description;
    }

    public Long getEvidenceSeq() {
        return evidenceSeq;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public Instant getRemediatedAt() {
        return remediatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public String getRemediationNote() {
        return remediationNote;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    void acknowledge(Instant at) {
        this.status = Status.ACKNOWLEDGED.name();
        this.acknowledgedAt = at;
    }

    void remediate(Instant at, String note) {
        this.status = Status.REMEDIATED.name();
        this.remediatedAt = at;
        this.remediationNote = note;
    }

    void close(Instant at) {
        this.status = Status.CLOSED.name();
        this.closedAt = at;
    }

    void markNotified(Instant at) {
        this.notifiedAt = at;
    }

    void linkEvidence(Long seq) {
        this.evidenceSeq = seq;
    }
}
