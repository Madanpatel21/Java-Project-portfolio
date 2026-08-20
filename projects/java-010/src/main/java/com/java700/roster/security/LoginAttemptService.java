package com.java700.roster.security;

import com.java700.roster.common.audit.AuditLogService;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Progressive account lockout for the local identity provider. */
@Service
public class LoginAttemptService {

    private final LocalUserRepository repository;
    private final AuditLogService audit;
    private final Clock clock;
    private final int maxAttempts;
    private final int lockMinutes;

    public LoginAttemptService(LocalUserRepository repository, AuditLogService audit, Clock clock,
                               @Value("${app.security.lockout.max-attempts:5}") int maxAttempts,
                               @Value("${app.security.lockout.lock-minutes:15}") int lockMinutes) {
        this.repository = repository;
        this.audit = audit;
        this.clock = clock;
        this.maxAttempts = maxAttempts;
        this.lockMinutes = lockMinutes;
    }

    @Transactional
    public void onFailure(LocalUser user) {
        int attempts = user.getFailedAttempts() + 1;
        Instant lockUntil = attempts >= maxAttempts
                ? Instant.now(clock).plus(lockMinutes, ChronoUnit.MINUTES) : null;
        user.registerFailure(attempts, lockUntil);
        repository.save(user);
        audit.record("AUTH_FAILURE", "LOCAL_USER", user.getId(),
                "Failed login attempt " + attempts + " of " + maxAttempts
                        + (lockUntil != null ? " (locked until " + lockUntil + ")" : ""));
    }

    @Transactional
    public void onSuccess(LocalUser user) {
        user.resetFailures();
        repository.save(user);
    }

    public boolean isLocked(LocalUser user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now(clock));
    }
}
