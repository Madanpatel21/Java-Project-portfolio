package com.java700.workforce.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Local identity-provider account (dev profile + fallback login); Argon2id password hashes. */
@Entity
@Table(name = "local_users")
public class LocalUser {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "username", nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "org_unit", nullable = false, length = 64)
    private String orgUnit;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LocalUser() {
    }

    public LocalUser(String id, String username, String passwordHash, String email,
                     String orgUnit, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.orgUnit = orgUnit;
        this.enabled = true;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public boolean isEnabled() {
        return enabled;
    }

    void registerFailure(int newFailedAttempts, Instant newLockedUntil) {
        this.failedAttempts = newFailedAttempts;
        this.lockedUntil = newLockedUntil;
    }

    void resetFailures() {
        this.failedAttempts = 0;
        this.lockedUntil = null;
    }
}
