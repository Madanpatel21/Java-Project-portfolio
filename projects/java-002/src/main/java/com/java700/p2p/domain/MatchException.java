package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A matching exception requiring AP resolution. */
@Entity
@Table(name = "exceptions")
public class MatchException {

    public enum Type {
        NO_PO_MATCH, MISSING_RECEIPT, PRICE_VARIANCE, QUANTITY_VARIANCE,
        DUPLICATE_INVOICE, OVER_BILLING
    }

    public enum Severity {
        WARNING, CRITICAL
    }

    public enum Status {
        OPEN, RESOLVED, WAIVED, REJECTED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "invoice_id", nullable = false, length = 36)
    private String invoiceId;

    @Column(name = "exception_type", nullable = false, length = 32)
    private String exceptionType;

    @Column(name = "severity", nullable = false, length = 16)
    private String severity;

    @Column(name = "detail_json", nullable = false, length = 4000)
    private String detailJson;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "assigned_to", length = 120)
    private String assignedTo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by", length = 120)
    private String resolvedBy;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    protected MatchException() {
    }

    public MatchException(String id, String invoiceId, Type type, Severity severity,
                          String detailJson, Instant createdAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.exceptionType = type.name();
        this.severity = severity.name();
        this.detailJson = detailJson;
        this.status = Status.OPEN.name();
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public Type getType() {
        return Type.valueOf(exceptionType);
    }

    public Severity getSeverity() {
        return Severity.valueOf(severity);
    }

    public String getDetailJson() {
        return detailJson;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void assign(String newAssignee) {
        this.assignedTo = newAssignee;
    }

    public void resolve(Status newStatus, String newResolvedBy, Instant newResolvedAt, String note) {
        this.status = newStatus.name();
        this.resolvedBy = newResolvedBy;
        this.resolvedAt = newResolvedAt;
        this.resolutionNote = note;
    }
}
