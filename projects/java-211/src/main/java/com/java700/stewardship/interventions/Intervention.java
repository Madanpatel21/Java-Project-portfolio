package com.java700.stewardship.interventions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Pharmacist intervention with prescriber acceptance workflow. */
@Entity
@Table(name = "interventions")
public class Intervention {

    public enum Status {
        PROPOSED, ACCEPTED, REJECTED, IGNORED, EXPIRED
    }

    public static final int EXPIRY_DAYS = 2;

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "prescription_id", nullable = false, length = 36)
    private String prescriptionId;

    @Column(name = "review_task_id", length = 36)
    private String reviewTaskId;

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "detail_json", nullable = false, length = 2000)
    private String detailJson;

    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "proposed_by", nullable = false, length = 120)
    private String proposedBy;

    @Column(name = "proposed_at", nullable = false)
    private Instant proposedAt;

    @Column(name = "decided_by", length = 120)
    private String decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "prescriber_response", length = 1000)
    private String prescriberResponse;

    protected Intervention() {
    }

    public Intervention(String id, String prescriptionId, String reviewTaskId, String type,
                        String detailJson, String reason, String proposedBy, Instant proposedAt) {
        this.id = id;
        this.prescriptionId = prescriptionId;
        this.reviewTaskId = reviewTaskId;
        this.type = type;
        this.detailJson = detailJson;
        this.reason = reason;
        this.status = Status.PROPOSED.name();
        this.proposedBy = proposedBy;
        this.proposedAt = proposedAt;
    }

    public String getId() {
        return id;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public String getReviewTaskId() {
        return reviewTaskId;
    }

    public String getType() {
        return type;
    }

    public String getDetailJson() {
        return detailJson;
    }

    public String getReason() {
        return reason;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public String getProposedBy() {
        return proposedBy;
    }

    public Instant getProposedAt() {
        return proposedAt;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public String getPrescriberResponse() {
        return prescriberResponse;
    }

    void decide(Status newStatus, String newDecidedBy, Instant newDecidedAt, String response) {
        this.status = newStatus.name();
        this.decidedBy = newDecidedBy;
        this.decidedAt = newDecidedAt;
        this.prescriberResponse = response;
    }

    void expire(Instant at) {
        this.status = Status.EXPIRED.name();
        this.decidedAt = at;
    }
}
