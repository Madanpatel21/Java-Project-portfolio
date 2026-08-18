package com.java700.crvs.certificates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Issued civil-status certificate; verifiable by token, revocable with audit. */
@Entity
@Table(name = "certificates")
public class Certificate {

    public enum Type {
        BIRTH, MARRIAGE, DEATH
    }

    public enum Status {
        VALID, REVOKED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "person_id", nullable = false, length = 36)
    private String personId;

    @Column(name = "type", nullable = false, length = 16)
    private String type;

    @Column(name = "token", nullable = false, unique = true, length = 32)
    private String token;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "issued_by", nullable = false, length = 120)
    private String issuedBy;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by", length = 120)
    private String revokedBy;

    @Column(name = "revoke_reason", length = 500)
    private String revokeReason;

    protected Certificate() {
    }

    public Certificate(String id, String personId, Type type, String token, String contentHash,
                       Instant issuedAt, String issuedBy) {
        this.id = id;
        this.personId = personId;
        this.type = type.name();
        this.token = token;
        this.contentHash = contentHash;
        this.status = Status.VALID.name();
        this.issuedAt = issuedAt;
        this.issuedBy = issuedBy;
    }

    public String getId() {
        return id;
    }

    public String getPersonId() {
        return personId;
    }

    public Type getType() {
        return Type.valueOf(type);
    }

    public String getToken() {
        return token;
    }

    public String getContentHash() {
        return contentHash;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public String getRevokeReason() {
        return revokeReason;
    }

    void revoke(String newRevokedBy, Instant newRevokedAt, String reason) {
        this.status = Status.REVOKED.name();
        this.revokedBy = newRevokedBy;
        this.revokedAt = newRevokedAt;
        this.revokeReason = reason;
    }
}
