package com.java700.stewardship.restricted;

import com.java700.stewardship.common.api.Problems;
import com.java700.stewardship.common.audit.AuditLogService;
import com.java700.stewardship.messaging.DomainEvent;
import com.java700.stewardship.messaging.DomainEventBus;
import com.java700.stewardship.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pre-authorization workflow for restricted antimicrobials. Approvals are time-boxed
 * (default 72h); expiry stops the underlying prescription via domain events.
 */
@Service
public class RestrictedAuthorizationService {

    private final RestrictedAuthRepository repository;
    private final DomainEventBus bus;
    private final AuditLogService audit;
    private final Clock clock;

    public RestrictedAuthorizationService(RestrictedAuthRepository repository, DomainEventBus bus,
                                          AuditLogService audit, Clock clock) {
        this.repository = repository;
        this.bus = bus;
        this.audit = audit;
        this.clock = clock;
    }

    /** Creates a PENDING authorization and returns its id. */
    @Transactional
    public String request(String prescriptionId, String indication) {
        RestrictedAuthorization auth = new RestrictedAuthorization(UUID.randomUUID().toString(),
                prescriptionId, SecurityUtil.currentUsername(), Instant.now(clock), indication);
        repository.save(auth);
        audit.record("RESTRICTED_AUTH_REQUESTED", "RESTRICTED_AUTH", auth.getId(),
                "Pre-authorization requested for prescription " + prescriptionId);
        bus.publish(new RestrictedAuthRequested(UUID.randomUUID().toString(), Instant.now(clock),
                auth.getId(), prescriptionId));
        return auth.getId();
    }

    @Transactional
    public RestrictedAuthApi.AuthView approve(String authId, int ttlHours) {
        RestrictedAuthorization auth = load(authId);
        if (auth.getStatus() != RestrictedAuthorization.Status.PENDING) {
            throw new Problems.Conflict("Authorization is already decided");
        }
        Instant now = Instant.now(clock);
        auth.approve(SecurityUtil.currentUsername(), now, now.plus(ttlHours, ChronoUnit.HOURS));
        repository.save(auth);
        audit.record("RESTRICTED_AUTH_APPROVED", "RESTRICTED_AUTH", authId,
                "Approved for " + ttlHours + "h by " + SecurityUtil.currentUsername());
        bus.publish(new RestrictedAuthApproved(UUID.randomUUID().toString(), now, authId,
                auth.getPrescriptionId(), auth.getExpiresAt()));
        return RestrictedAuthApi.AuthView.from(auth);
    }

    @Transactional
    public RestrictedAuthApi.AuthView reject(String authId, String note) {
        RestrictedAuthorization auth = load(authId);
        if (auth.getStatus() != RestrictedAuthorization.Status.PENDING) {
            throw new Problems.Conflict("Authorization is already decided");
        }
        auth.reject(SecurityUtil.currentUsername(), Instant.now(clock), note);
        repository.save(auth);
        audit.record("RESTRICTED_AUTH_REJECTED", "RESTRICTED_AUTH", authId,
                "Rejected: " + note);
        return RestrictedAuthApi.AuthView.from(auth);
    }

    @Transactional(readOnly = true)
    public List<RestrictedAuthApi.AuthView> pending() {
        return repository.findByStatus("PENDING").stream()
                .map(RestrictedAuthApi.AuthView::from).toList();
    }

    /** Expires APPROVED authorizations past their window. */
    @Transactional
    public int expireDue(int defaultTtlHours) {
        List<RestrictedAuthorization> due = repository.findByStatusAndExpiresAtBefore(
                "APPROVED", Instant.now(clock));
        for (RestrictedAuthorization auth : due) {
            auth.expire(Instant.now(clock));
            repository.save(auth);
            bus.publish(new RestrictedAuthExpired(UUID.randomUUID().toString(), Instant.now(clock),
                    auth.getId(), auth.getPrescriptionId()));
        }
        return due.size();
    }

    private RestrictedAuthorization load(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Authorization not found"));
    }

    public record RestrictedAuthRequested(String eventId, Instant occurredAt, String authId,
                                          String prescriptionId) implements DomainEvent {
    }

    public record RestrictedAuthApproved(String eventId, Instant occurredAt, String authId,
                                         String prescriptionId, Instant expiresAt)
            implements DomainEvent {
    }

    public record RestrictedAuthExpired(String eventId, Instant occurredAt, String authId,
                                        String prescriptionId) implements DomainEvent {
    }
}
