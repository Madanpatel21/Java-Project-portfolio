package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** Simulated GL posting (debit GRNI / credit AP) produced by the posting batch. */
@Entity
@Table(name = "gl_postings")
public class GlPosting {

    public enum Status {
        PENDING, POSTED, FAILED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "invoice_id", nullable = false, length = 36)
    private String invoiceId;

    @Column(name = "batch_id", length = 36)
    private String batchId;

    @Column(name = "gl_account", nullable = false, length = 32)
    private String glAccount;

    @Column(name = "debit", nullable = false, precision = 14, scale = 2)
    private BigDecimal debit;

    @Column(name = "credit", nullable = false, precision = 14, scale = 2)
    private BigDecimal credit;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "posted_at")
    private Instant postedAt;

    protected GlPosting() {
    }

    public GlPosting(String id, String invoiceId, String batchId, String glAccount,
                     BigDecimal debit, BigDecimal credit, Instant createdAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.batchId = batchId;
        this.glAccount = glAccount;
        this.debit = debit;
        this.credit = credit;
        this.status = Status.PENDING.name();
        this.createdAt = createdAt;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getGlAccount() {
        return glAccount;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public void post(Instant at) {
        this.status = Status.POSTED.name();
        this.postedAt = at;
    }
}
