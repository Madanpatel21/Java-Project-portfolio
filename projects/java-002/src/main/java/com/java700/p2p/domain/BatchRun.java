package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A reconciliation batch run. */
@Entity
@Table(name = "batch_runs")
public class BatchRun {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "invoices_processed", nullable = false)
    private int invoicesProcessed;

    @Column(name = "exceptions_created", nullable = false)
    private int exceptionsCreated;

    @Column(name = "postings_created", nullable = false)
    private int postingsCreated;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    protected BatchRun() {
    }

    public BatchRun(String id, Instant startedAt) {
        this.id = id;
        this.startedAt = startedAt;
        this.status = "RUNNING";
    }

    public String getId() {
        return id;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public int getInvoicesProcessed() {
        return invoicesProcessed;
    }

    public int getExceptionsCreated() {
        return exceptionsCreated;
    }

    public int getPostingsCreated() {
        return postingsCreated;
    }

    public String getStatus() {
        return status;
    }

    public void complete(int processed, int exceptions, int postings, Instant at) {
        this.status = "COMPLETED";
        this.invoicesProcessed = processed;
        this.exceptionsCreated = exceptions;
        this.postingsCreated = postings;
        this.completedAt = at;
    }
}
