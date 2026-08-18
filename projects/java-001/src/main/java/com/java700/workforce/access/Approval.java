package com.java700.workforce.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** One approver's decision; dual control requires two distinct approvers. */
@Entity
@Table(name = "approval")
public class Approval {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "access_request_id", nullable = false, length = 36)
    private String accessRequestId;

    @Column(name = "approver_id", nullable = false, length = 36)
    private String approverId;

    @Column(name = "approver_name", nullable = false, length = 120)
    private String approverName;

    @Column(name = "decision", nullable = false, length = 16)
    private String decision;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    protected Approval() {
    }

    public Approval(String id, String accessRequestId, String approverId, String approverName,
                    String decision, String comment, Instant decidedAt) {
        this.id = id;
        this.accessRequestId = accessRequestId;
        this.approverId = approverId;
        this.approverName = approverName;
        this.decision = decision;
        this.comment = comment;
        this.decidedAt = decidedAt;
    }

    public String getApproverId() {
        return approverId;
    }

    public String getDecision() {
        return decision;
    }
}
