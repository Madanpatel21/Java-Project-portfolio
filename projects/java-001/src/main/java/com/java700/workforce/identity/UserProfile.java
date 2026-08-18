package com.java700.workforce.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Workforce user profile (directory record). PII is masked at the API boundary. */
@Entity
@Table(name = "users")
public class UserProfile {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "username", nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "org_unit", nullable = false, length = 64)
    private String orgUnit;

    @Column(name = "cert_expires_at")
    private Instant certExpiresAt;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserProfile() {
    }

    public UserProfile(String id, String username, String email, String orgUnit,
                       Instant certExpiresAt, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.orgUnit = orgUnit;
        this.certExpiresAt = certExpiresAt;
        this.status = "ACTIVE";
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public Instant getCertExpiresAt() {
        return certExpiresAt;
    }

    public String getStatus() {
        return status;
    }
}
