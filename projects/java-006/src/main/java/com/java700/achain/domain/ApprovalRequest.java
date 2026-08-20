package com.java700.achain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/** A request flowing through an approval chain, bound to a policy version snapshot. */
@Entity
@Table(name = "approval_requests")
public class ApprovalRequest {

    public enum Status {
        PENDING, APPROVED, REJECTED, CANCELLED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "chain_id", nullable = false, length = 36)
    private String chainId;

    @Column(name = "policy_version_id", nullable = false, length = 36)
    private String policyVersionId;

    @Column(name = "subject_type", nullable = false, length = 64)
    private String subjectType;

    @Column(name = "subject_id", nullable = false, length = 64)
    private String subjectId;

    @Column(name = "payload_json", nullable = false, length = 8192)
    private String payloadJson;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "current_step", nullable = false)
    private int currentStep;

    @Column(name = "requested_by_id", nullable = false, length = 36)
    private String requestedById;

    @Column(name = "requested_by_name", nullable = false, length = 120)
    private String requestedByName;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected ApprovalRequest() {
    }

    public ApprovalRequest(String id, String chainId, String policyVersionId, String subjectType,
                           String subjectId, String payloadJson, String requestedById,
                           String requestedByName, Instant dueAt, Instant createdAt) {
        this.id = id;
        this.chainId = chainId;
        this.policyVersionId = policyVersionId;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.payloadJson = payloadJson;
        this.status = Status.PENDING.name();
        this.currentStep = 1;
        this.requestedById = requestedById;
        this.requestedByName = requestedByName;
        this.dueAt = dueAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getChainId() {
        return chainId;
    }

    public String getPolicyVersionId() {
        return policyVersionId;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public String getRequestedById() {
        return requestedById;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void advanceStep() {
        this.currentStep++;
    }

    public void finish(Status newStatus, Instant at) {
        this.status = newStatus.name();
        this.decidedAt = at;
    }

    public void extendDue(Instant at) {
        this.dueAt = at;
    }
}
