package com.java700.workforce.access;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.List;

/** Active access grant: roles on a resource, with expiry and recertification windows. */
@Entity
@Table(name = "access_grant")
public class Grant {

    public enum Status {
        ACTIVE, EXPIRED, REVOKED
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "resource_name", nullable = false, length = 120)
    private String resourceName;

    @Column(name = "roles_json", nullable = false, length = 1024)
    private String rolesJson;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by", length = 120)
    private String revokedBy;

    @Column(name = "revoke_reason", length = 1000)
    private String revokeReason;

    @Column(name = "recert_due_at")
    private Instant recertDueAt;

    @Column(name = "recertified_at")
    private Instant recertifiedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Grant() {
    }

    public Grant(String id, String userId, String resourceType, String resourceName,
                 String rolesJson, Instant grantedAt, Instant expiresAt, Instant recertDueAt) {
        this.id = id;
        this.userId = userId;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.rolesJson = rolesJson;
        this.status = Status.ACTIVE.name();
        this.grantedAt = grantedAt;
        this.expiresAt = expiresAt;
        this.recertDueAt = recertDueAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRecertDueAt() {
        return recertDueAt;
    }

    public Instant getRecertifiedAt() {
        return recertifiedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getRevokeReason() {
        return revokeReason;
    }

    public String getRolesJson() {
        return rolesJson;
    }

    public List<String> roles() {
        try {
            return MAPPER.readValue(rolesJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    public void revoke(Instant at, String by, String reason) {
        this.status = Status.REVOKED.name();
        this.revokedAt = at;
        this.revokedBy = by;
        this.revokeReason = reason;
    }

    public void expire(Instant at) {
        this.status = Status.EXPIRED.name();
        this.revokedAt = at;
    }

    public void recertify(Instant newAt) {
        this.recertifiedAt = newAt;
    }

    public void extendRecertDue(Instant due) {
        this.recertDueAt = due;
    }
}
