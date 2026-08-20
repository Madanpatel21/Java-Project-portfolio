package com.java700.expfraud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A four-eyes investigation case opened for a high-risk claim. */
@Entity
@Table(name = "fraud_cases")
public class FraudCase {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_REVIEWED = "REVIEWED";
    public static final String STATUS_CONFIRMED_FRAUD = "CONFIRMED_FRAUD";
    public static final String STATUS_CLEARED = "CLEARED";

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "case_no", length = 32, nullable = false, unique = true)
    private String caseNo;

    @Column(name = "claim_id", length = 36, nullable = false)
    private String claimId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "reasons_json", length = 4000, nullable = false)
    private String reasonsJson;

    @Column(name = "evidence_json", length = 4000)
    private String evidenceJson;

    @Column(length = 24, nullable = false)
    private String status;

    @Column(name = "opened_by", length = 64, nullable = false)
    private String openedBy;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "reviewer_one", length = 64)
    private String reviewerOne;

    @Column(name = "reviewer_one_note", length = 1000)
    private String reviewerOneNote;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewer_two", length = 64)
    private String reviewerTwo;

    @Column(length = 24)
    private String decision;

    @Column(name = "decision_note", length = 1000)
    private String decisionNote;

    @Column(name = "decided_at")
    private Instant decidedAt;

    protected FraudCase() {
    }

    public FraudCase(String id, String caseNo, String claimId, int riskScore, String reasonsJson,
                     String evidenceJson, String status, String openedBy, Instant openedAt) {
        this.id = id;
        this.caseNo = caseNo;
        this.claimId = claimId;
        this.riskScore = riskScore;
        this.reasonsJson = reasonsJson;
        this.evidenceJson = evidenceJson;
        this.status = status;
        this.openedBy = openedBy;
        this.openedAt = openedAt;
    }

    public void firstReview(String reviewer, String note, Instant at) {
        this.reviewerOne = reviewer;
        this.reviewerOneNote = note;
        this.reviewedAt = at;
        this.status = STATUS_REVIEWED;
    }

    public void finalDecision(String reviewer, String newDecision, String note, Instant at) {
        this.reviewerTwo = reviewer;
        this.decision = newDecision;
        this.decisionNote = note;
        this.decidedAt = at;
        this.status = "CONFIRM_FRAUD".equals(newDecision) ? STATUS_CONFIRMED_FRAUD : STATUS_CLEARED;
    }

    public String getId() {
        return id;
    }

    public String getCaseNo() {
        return caseNo;
    }

    public String getClaimId() {
        return claimId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getReasonsJson() {
        return reasonsJson;
    }

    public String getEvidenceJson() {
        return evidenceJson;
    }

    public String getStatus() {
        return status;
    }

    public String getOpenedBy() {
        return openedBy;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public String getReviewerOne() {
        return reviewerOne;
    }

    public String getReviewerOneNote() {
        return reviewerOneNote;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public String getReviewerTwo() {
        return reviewerTwo;
    }

    public String getDecision() {
        return decision;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
