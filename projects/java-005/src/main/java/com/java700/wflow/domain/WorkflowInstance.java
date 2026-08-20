package com.java700.wflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/** A running (or finished) workflow instance pinned to its definition version. */
@Entity
@Table(name = "workflow_instances")
public class WorkflowInstance {

    public enum Status {
        RUNNING, WAITING_TASK, WAITING_TIMER, COMPLETED, FAILED, CANCELLED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "definition_id", nullable = false, length = 36)
    private String definitionId;

    @Column(name = "business_key", nullable = false, length = 120)
    private String businessKey;

    @Column(name = "status", nullable = false, length = 24)
    private String status;

    @Column(name = "variables_json", nullable = false, length = 8192)
    private String variablesJson;

    @Column(name = "current_node_id", length = 64)
    private String currentNodeId;

    @Column(name = "resume_at")
    private Instant resumeAt;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected WorkflowInstance() {
    }

    public WorkflowInstance(String id, String definitionId, String businessKey,
                            String variablesJson, Instant startedAt) {
        this.id = id;
        this.definitionId = definitionId;
        this.businessKey = businessKey;
        this.variablesJson = variablesJson;
        this.status = Status.RUNNING.name();
        this.startedAt = startedAt;
    }

    public String getId() {
        return id;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public String getVariablesJson() {
        return variablesJson;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public Instant getResumeAt() {
        return resumeAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setState(Status newStatus, String nodeId) {
        this.status = newStatus.name();
        this.currentNodeId = nodeId;
    }

    public void updateVariables(String newVariablesJson) {
        this.variablesJson = newVariablesJson;
    }

    public void setResumeAt(Instant at) {
        this.resumeAt = at;
    }

    public void finish(Status newStatus, Instant at) {
        this.status = newStatus.name();
        this.completedAt = at;
    }
}
