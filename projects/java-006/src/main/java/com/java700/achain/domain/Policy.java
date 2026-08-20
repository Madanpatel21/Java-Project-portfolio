package com.java700.achain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A governance policy with exactly one active version. */
@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "policy_code", nullable = false, unique = true, length = 64)
    private String policyCode;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "active_version_id", length = 36)
    private String activeVersionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Policy() {
    }

    public Policy(String id, String policyCode, String name, String description, Instant createdAt) {
        this.id = id;
        this.policyCode = policyCode;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getActiveVersionId() {
        return activeVersionId;
    }

    public void activateVersion(String versionId) {
        this.activeVersionId = versionId;
    }
}
