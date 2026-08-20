package com.java700.wflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Append-only execution trace of a workflow instance. */
@Entity
@Table(name = "workflow_steps")
public class WorkflowStep {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "instance_id", nullable = false, length = 36)
    private String instanceId;

    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    @Column(name = "step_type", nullable = false, length = 16)
    private String stepType;

    @Column(name = "result_json", length = 2000)
    private String resultJson;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected WorkflowStep() {
    }

    public WorkflowStep(String id, String instanceId, String nodeId, String stepType,
                        String resultJson, Instant occurredAt) {
        this.id = id;
        this.instanceId = instanceId;
        this.nodeId = nodeId;
        this.stepType = stepType;
        this.resultJson = resultJson;
        this.occurredAt = occurredAt;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getStepType() {
        return stepType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
