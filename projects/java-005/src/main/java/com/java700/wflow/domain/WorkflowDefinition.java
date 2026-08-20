package com.java700.wflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A versioned, model-driven workflow definition. New versions deprecate the previous. */
@Entity
@Table(name = "workflow_definitions")
public class WorkflowDefinition {

    public enum Status {
        ACTIVE, DEPRECATED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "definition_key", nullable = false, length = 64)
    private String definitionKey;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(name = "definition_json", nullable = false, length = 8192)
    private String definitionJson;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "created_by", nullable = false, length = 120)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WorkflowDefinition() {
    }

    public WorkflowDefinition(String id, String definitionKey, String name, int versionNo,
                              String definitionJson, String createdBy, Instant createdAt) {
        this.id = id;
        this.definitionKey = definitionKey;
        this.name = name;
        this.versionNo = versionNo;
        this.definitionJson = definitionJson;
        this.status = Status.ACTIVE.name();
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getDefinitionKey() {
        return definitionKey;
    }

    public String getName() {
        return name;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public String getDefinitionJson() {
        return definitionJson;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void deprecate() {
        this.status = Status.DEPRECATED.name();
    }
}
