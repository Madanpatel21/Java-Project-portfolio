package com.java700.contracts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A governance approval (contract activation, obligation waiver). */
@Entity
@Table(name = "approvals")
public class Approval {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "target_type", nullable = false, length = 32)
    private String targetType;

    @Column(name = "target_id", nullable = false, length = 36)
    private String targetId;

    @Column(name = "approver_role", nullable = false, length = 32)
    private String approverRole;

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

    protected Approval() {
    }

    public Approval(String id, String targetType, String targetId, String approverRole,
                    String approverId, String approverName, String decision, String note,
                    Instant decidedAt) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.approverRole = approverRole;
        this.approverId = approverId;
        this.approverName = approverName;
        this.decision = decision;
        this.note = note;
        this.decidedAt = decidedAt;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getApproverRole() {
        return approverRole;
    }

    public String getApproverId() {
        return approverId;
    }

    public String getDecision() {
        return decision;
    }
}
