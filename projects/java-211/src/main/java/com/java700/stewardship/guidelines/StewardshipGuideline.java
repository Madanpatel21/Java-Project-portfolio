package com.java700.stewardship.guidelines;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Immutable versioned guideline set; activation supersedes the previous version. */
@Entity
@Table(name = "stewardship_guidelines")
public class StewardshipGuideline {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "created_by", nullable = false, length = 120)
    private String createdBy;

    @Column(name = "rules_json", nullable = false, length = 8192)
    private String rulesJson;

    protected StewardshipGuideline() {
    }

    public StewardshipGuideline(String id, String name, int versionNo, String status,
                                Instant effectiveFrom, String createdBy, String rulesJson) {
        this.id = id;
        this.name = name;
        this.versionNo = versionNo;
        this.status = status;
        this.effectiveFrom = effectiveFrom;
        this.createdBy = createdBy;
        this.rulesJson = rulesJson;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public String getStatus() {
        return status;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getRulesJson() {
        return rulesJson;
    }
}
