package com.java700.achain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** One approver's decision on one step — the audit-grade evidence record. */
@Entity
@Table(name = "approval_decisions")
public class ApprovalDecision {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "request_id", nullable = false, length = 36)
    private String requestId;

    @Column(name = "step_no", nullable = false)
    private int stepNo;

    @Column(name = "approver_id", nullable = false, length = 36)
    private String approverId;

    @Column(name = "approver_name", nullable = false, length = 120)
    private String approverName;

    @Column(name = "decision", nullable = false, length = 16)
    private String decision;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    protected ApprovalDecision() {
    }

    public ApprovalDecision(String id, String requestId, int stepNo, String approverId,
                            String approverName, String decision, String note, Instant decidedAt) {
        this.id = id;
        this.requestId = requestId;
        this.stepNo = stepNo;
        this.approverId = approverId;
        this.approverName = approverName;
        this.decision = decision;
        this.note = note;
        this.decidedAt = decidedAt;
    }

    public String getId() {
        return id;
    }

    public int getStepNo() {
        return stepNo;
    }

    public String getApproverId() {
        return approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public String getNote() {
        return note;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public String getDecision() {
        return decision;
    }
}
