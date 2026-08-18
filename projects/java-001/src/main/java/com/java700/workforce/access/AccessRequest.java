package com.java700.workforce.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Access request with dual-control approval state machine. */
@Entity
@Table(name = "access_request")
public class AccessRequest {

    public enum Status {
        PENDING, APPROVED, REJECTED, CANCELLED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "requester_id", nullable = false, length = 36)
    private String requesterId;

    @Column(name = "subject_user_id", nullable = false, length = 36)
    private String subjectUserId;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "resource_name", nullable = false, length = 120)
    private String resourceName;

    @Column(name = "roles_json", nullable = false, length = 1024)
    private String rolesJson;

    @Column(name = "justification", length = 2000)
    private String justification;

    @Column(name = "status", nullable = false, length = 24)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decided_by", length = 120)
    private String decidedBy;

    @Column(name = "decision_note", length = 1000)
    private String decisionNote;

    protected AccessRequest() {
    }

    public AccessRequest(String id, String requesterId, String subjectUserId, String resourceType,
                         String resourceName, String rolesJson, String justification, Instant createdAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.subjectUserId = subjectUserId;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.rolesJson = rolesJson;
        this.justification = justification;
        this.status = Status.PENDING.name();
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getSubjectUserId() {
        return subjectUserId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getRolesJson() {
        return rolesJson;
    }

    public String getJustification() {
        return justification;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    void markDecided(Status newStatus, Instant newDecidedAt, String newDecidedBy, String note) {
        this.status = newStatus.name();
        this.decidedAt = newDecidedAt;
        this.decidedBy = newDecidedBy;
        this.decisionNote = note;
    }
}
