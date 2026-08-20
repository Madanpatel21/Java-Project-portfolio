package com.java700.wflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/** A human (or automated) task created by the workflow engine. */
@Entity
@Table(name = "workflow_tasks")
public class WorkflowTask {

    public enum Type {
        APPROVAL, AUTOMATED, COMPENSATION
    }

    public enum Status {
        PENDING, COMPLETED, SKIPPED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "instance_id", nullable = false, length = 36)
    private String instanceId;

    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    @Column(name = "task_type", nullable = false, length = 16)
    private String taskType;

    @Column(name = "assignee_role", length = 32)
    private String assigneeRole;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "result_json", length = 2000)
    private String resultJson;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "completed_by", length = 120)
    private String completedBy;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected WorkflowTask() {
    }

    public WorkflowTask(String id, String instanceId, String nodeId, Type type,
                        String assigneeRole, Instant dueAt, Instant createdAt) {
        this.id = id;
        this.instanceId = instanceId;
        this.nodeId = nodeId;
        this.taskType = type.name();
        this.assigneeRole = assigneeRole;
        this.status = Status.PENDING.name();
        this.dueAt = dueAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Type getType() {
        return Type.valueOf(taskType);
    }

    public String getAssigneeRole() {
        return assigneeRole;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public String getResultJson() {
        return resultJson;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void complete(String newResultJson, String newCompletedBy, Instant at) {
        this.status = Status.COMPLETED.name();
        this.resultJson = newResultJson;
        this.completedBy = newCompletedBy;
        this.completedAt = at;
    }

    public void skip(Instant at) {
        this.status = Status.SKIPPED.name();
        this.completedAt = at;
    }

    public void escalate(Instant newDueAt) {
        this.dueAt = newDueAt;
    }
}
