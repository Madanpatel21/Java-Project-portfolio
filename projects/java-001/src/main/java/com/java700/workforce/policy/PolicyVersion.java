package com.java700.workforce.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Immutable snapshot of a policy's rule set at a version. */
@Entity
@Table(name = "policy_version")
public class PolicyVersion {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "policy_id", nullable = false, length = 36)
    private String policyId;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(name = "rules_json", nullable = false, length = 8192)
    private String rulesJson;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "created_by", nullable = false, length = 120)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PolicyVersion() {
    }

    public PolicyVersion(String id, String policyId, int versionNo, String rulesJson, String status,
                         Instant effectiveFrom, String createdBy, Instant createdAt) {
        this.id = id;
        this.policyId = policyId;
        this.versionNo = versionNo;
        this.rulesJson = rulesJson;
        this.status = status;
        this.effectiveFrom = effectiveFrom;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getPolicyId() {
        return policyId;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public String getRulesJson() {
        return rulesJson;
    }

    public String getStatus() {
        return status;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
