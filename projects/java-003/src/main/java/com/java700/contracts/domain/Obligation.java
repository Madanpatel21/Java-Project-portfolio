package com.java700.contracts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/** An obligation extracted from a contract clause, with SLA windows and recurrence. */
@Entity
@Table(name = "obligations")
public class Obligation {

    public enum Type {
        RENEWAL, PAYMENT, DELIVERY, EXIT_RIGHT, COMPLIANCE, INSURANCE, OTHER
    }

    public enum Criticality {
        LOW, MEDIUM, HIGH
    }

    public enum Status {
        OPEN, NOTIFIED, ACKNOWLEDGED, COMPLETED, WAIVED, OVERDUE
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "contract_id", nullable = false, length = 36)
    private String contractId;

    @Column(name = "source_clause", length = 32)
    private String sourceClause;

    @Column(name = "obligation_type", nullable = false, length = 32)
    private String obligationType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "window_before_days", nullable = false)
    private int windowBeforeDays;

    @Column(name = "repeat_interval_days")
    private Integer repeatIntervalDays;

    @Column(name = "criticality", nullable = false, length = 8)
    private String criticality;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "assigned_to", length = 120)
    private String assignedTo;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "waived_at")
    private Instant waivedAt;

    @Column(name = "waived_by", length = 120)
    private String waivedBy;

    @Column(name = "waiver_reason", length = 1000)
    private String waiverReason;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    @Column(name = "overdue_at")
    private Instant overdueAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Obligation() {
    }

    public Obligation(String id, String contractId, String sourceClause, Type type, String title,
                      String description, Instant dueAt, int windowBeforeDays,
                      Integer repeatIntervalDays, Criticality criticality, String assignedTo,
                      Instant createdAt) {
        this.id = id;
        this.contractId = contractId;
        this.sourceClause = sourceClause;
        this.obligationType = type.name();
        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
        this.windowBeforeDays = windowBeforeDays;
        this.repeatIntervalDays = repeatIntervalDays;
        this.criticality = criticality.name();
        this.status = Status.OPEN.name();
        this.assignedTo = assignedTo;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getContractId() {
        return contractId;
    }

    public String getSourceClause() {
        return sourceClause;
    }

    public Type getType() {
        return Type.valueOf(obligationType);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public int getWindowBeforeDays() {
        return windowBeforeDays;
    }

    public Integer getRepeatIntervalDays() {
        return repeatIntervalDays;
    }

    public Criticality getCriticality() {
        return Criticality.valueOf(criticality);
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getWaivedAt() {
        return waivedAt;
    }

    public String getWaivedBy() {
        return waivedBy;
    }

    public String getWaiverReason() {
        return waiverReason;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    public Instant getOverdueAt() {
        return overdueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void notify(Instant at) {
        this.status = Status.NOTIFIED.name();
        this.notifiedAt = at;
    }

    public void acknowledge(Instant at) {
        this.status = Status.ACKNOWLEDGED.name();
        this.acknowledgedAt = at;
    }

    public void complete(Instant at) {
        this.status = Status.COMPLETED.name();
        this.completedAt = at;
    }

    public void waive(String newWaivedBy, Instant at, String reason) {
        this.status = Status.WAIVED.name();
        this.waivedBy = newWaivedBy;
        this.waivedAt = at;
        this.waiverReason = reason;
    }

    public void markOverdue(Instant at) {
        this.status = Status.OVERDUE.name();
        this.overdueAt = at;
    }

    public void assign(String newAssignee) {
        this.assignedTo = newAssignee;
    }
}
