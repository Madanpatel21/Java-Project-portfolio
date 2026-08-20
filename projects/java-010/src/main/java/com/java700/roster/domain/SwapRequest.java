package com.java700.roster.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** SwapRequest. */
@Entity
@Table(name = "swap_requests")
public class SwapRequest {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    public void decide(String newStatus, String reviewer, Instant at) {
        this.status = newStatus;
        this.reviewedBy = reviewer;
        this.reviewedAt = at;
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "swap_no", length = 32, nullable = false, unique = true)
    private String swapNo;
    @Column(name = "assignment_id", length = 36, nullable = false)
    private String assignmentId;
    @Column(name = "requested_by", length = 36, nullable = false)
    private String requestedBy;
    @Column(name = "target_employee_id", length = 36, nullable = false)
    private String targetEmployeeId;
    @Column(length = 300)
    private String reason;
    @Column(length = 16, nullable = false)
    private String status;
    @Column(name = "reviewed_by", length = 64)
    private String reviewedBy;
    @Column(name = "reviewed_at")
    private Instant reviewedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SwapRequest() {
    }

    public SwapRequest(String id, String swapNo, String assignmentId, String requestedBy, String targetEmployeeId, String reason, String status, String reviewedBy, Instant reviewedAt, Instant createdAt) {
        this.id = id;
        this.swapNo = swapNo;
        this.assignmentId = assignmentId;
        this.requestedBy = requestedBy;
        this.targetEmployeeId = targetEmployeeId;
        this.reason = reason;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt;

    }

    public String getId() {
        return id;
    }

    public String getSwapNo() {
        return swapNo;
    }
    public String getAssignmentId() {
        return assignmentId;
    }
    public String getRequestedBy() {
        return requestedBy;
    }
    public String getTargetEmployeeId() {
        return targetEmployeeId;
    }
    public String getReason() {
        return reason;
    }
    public String getStatus() {
        return status;
    }
    public String getReviewedBy() {
        return reviewedBy;
    }
    public Instant getReviewedAt() {
        return reviewedAt;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
