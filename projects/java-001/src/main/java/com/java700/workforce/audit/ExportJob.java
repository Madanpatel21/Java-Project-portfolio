package com.java700.workforce.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Asynchronous auditor-grade export job: JSONL evidence bundle + HMAC signature + manifest. */
@Entity
@Table(name = "export_job")
public class ExportJob {

    public enum Status {
        PENDING, COMPLETED, FAILED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "requested_by", nullable = false, length = 120)
    private String requestedBy;

    @Column(name = "scope_user_id", length = 36)
    private String scopeUserId;

    @Column(name = "range_from", nullable = false)
    private Instant rangeFrom;

    @Column(name = "range_to", nullable = false)
    private Instant rangeTo;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "start_seq")
    private Long startSeq;

    @Column(name = "end_seq")
    private Long endSeq;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "hmac", length = 128)
    private String hmac;

    @Column(name = "error", length = 2000)
    private String error;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ExportJob() {
    }

    public ExportJob(String id, String requestedBy, String scopeUserId, Instant rangeFrom,
                     Instant rangeTo, Long startSeq, Instant createdAt) {
        this.id = id;
        this.requestedBy = requestedBy;
        this.scopeUserId = scopeUserId;
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
        this.startSeq = startSeq;
        this.status = Status.PENDING.name();
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public String getScopeUserId() {
        return scopeUserId;
    }

    public Instant getRangeFrom() {
        return rangeFrom;
    }

    public Instant getRangeTo() {
        return rangeTo;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public Long getStartSeq() {
        return startSeq;
    }

    public Long getEndSeq() {
        return endSeq;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getHmac() {
        return hmac;
    }

    public String getError() {
        return error;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    void complete(Long newEndSeq, String newFilePath, String newHmac, Instant at) {
        this.status = Status.COMPLETED.name();
        this.endSeq = newEndSeq;
        this.filePath = newFilePath;
        this.hmac = newHmac;
        this.completedAt = at;
    }

    void fail(String newError, Instant at) {
        this.status = Status.FAILED.name();
        this.error = newError;
        this.completedAt = at;
    }
}
