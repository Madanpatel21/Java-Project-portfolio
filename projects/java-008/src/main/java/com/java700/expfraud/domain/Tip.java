package com.java700.expfraud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Anonymous whistleblower tip. No submitter identity is ever captured. */
@Entity
@Table(name = "tips")
public class Tip {

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    public static final String STATUS_CLOSED = "CLOSED";

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "tip_no", length = 32, nullable = false, unique = true)
    private String tipNo;

    @Column(length = 24, nullable = false)
    private String channel;

    @Column(length = 200, nullable = false)
    private String subject;

    @Column(length = 2000, nullable = false)
    private String description;

    @Column(name = "related_claim_no", length = 32)
    private String relatedClaimNo;

    @Column(length = 24, nullable = false)
    private String status;

    @Column(length = 500)
    private String outcome;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_by", length = 64)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected Tip() {
    }

    public Tip(String id, String tipNo, String channel, String subject, String description,
               String relatedClaimNo, String status, Instant submittedAt) {
        this.id = id;
        this.tipNo = tipNo;
        this.channel = channel;
        this.subject = subject;
        this.description = description;
        this.relatedClaimNo = relatedClaimNo;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public void review(String reviewer, String newOutcome, Instant at) {
        this.status = STATUS_CLOSED;
        this.outcome = newOutcome;
        this.reviewedBy = reviewer;
        this.reviewedAt = at;
    }

    public String getId() {
        return id;
    }

    public String getTipNo() {
        return tipNo;
    }

    public String getChannel() {
        return channel;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public String getRelatedClaimNo() {
        return relatedClaimNo;
    }

    public String getStatus() {
        return status;
    }

    public String getOutcome() {
        return outcome;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
