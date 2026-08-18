package com.java700.workforce.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Versioned compliance policy (e.g. ACCESS_GOVERNANCE). */
@Entity
@Table(name = "policy")
public class Policy {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "active_version_id", length = 36)
    private String activeVersionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Policy() {
    }

    public Policy(String id, String code, String name, String description, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
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

    void activateVersion(String versionId) {
        this.activeVersionId = versionId;
    }
}
