package com.java700.workforce.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.java700.workforce.common.audit.AuditLogService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    private LocalUserRepository repository;
    private LoginAttemptService service;
    private LocalUser user;

    @BeforeEach
    void setUp() {
        repository = mock(LocalUserRepository.class);
        AuditLogService audit = mock(AuditLogService.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-18T00:00:00Z"), ZoneOffset.UTC);
        service = new LoginAttemptService(repository, audit, clock, 5, 15);
        user = new LocalUser("u1", "alice", "hash", "a@x.io", "HR",
                Instant.parse("2026-08-18T00:00:00Z"));
    }

    @Test
    void locksAfterMaxAttempts() {
        for (int i = 1; i <= 5; i++) {
            service.onFailure(user);
        }
        assertThat(user.getFailedAttempts()).isEqualTo(5);
        assertThat(user.getLockedUntil())
                .isEqualTo(Instant.parse("2026-08-18T00:15:00Z"));
        assertThat(service.isLocked(user)).isTrue();
    }

    @Test
    void successResetsFailures() {
        service.onFailure(user);
        service.onFailure(user);
        service.onSuccess(user);
        assertThat(user.getFailedAttempts()).isZero();
        assertThat(service.isLocked(user)).isFalse();
    }

    @Test
    void lockExpiresAfterWindow() throws Exception {
        for (int i = 0; i < 5; i++) {
            service.onFailure(user);
        }
        LocalUserRepository repo2 = mock(LocalUserRepository.class);
        when(repo2.findByUsername(any())).thenReturn(Optional.of(user));
        // rebuild with a clock 16 minutes later
        Clock later = Clock.fixed(Instant.parse("2026-08-18T00:15:00Z").plus(1, ChronoUnit.MINUTES),
                ZoneOffset.UTC);
        LoginAttemptService laterService = new LoginAttemptService(repo2,
                mock(AuditLogService.class), later, 5, 15);
        assertThat(laterService.isLocked(user)).isFalse();
    }
}
