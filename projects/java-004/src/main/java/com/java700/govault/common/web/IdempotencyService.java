package com.java700.govault.common.web;

import com.java700.govault.common.api.Problems;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Idempotency-key handling for mutating endpoints (registrations, decisions, certificates). */
@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;
    private final Clock clock;

    public IdempotencyService(IdempotencyRecordRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String begin(String key, String resourceType) {
        if (key == null || key.isBlank() || key.length() > 200) {
            throw new Problems.BadRequest("Idempotency-Key header is required for this operation");
        }
        var existing = repository.findByKey(key);
        if (existing.isPresent()) {
            String resourceId = existing.get().getResourceId();
            if (resourceId != null) {
                return resourceId;
            }
            throw new Problems.Conflict("Request with this Idempotency-Key is still being processed");
        }
        try {
            repository.saveAndFlush(new IdempotencyRecord(UUID.randomUUID().toString(), key,
                    resourceType, null, 202, Instant.now(clock)));
        } catch (RuntimeException e) {
            throw new Problems.Conflict("Duplicate Idempotency-Key");
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(String key, String resourceId, int status) {
        repository.findByKey(key).ifPresent(rec ->
                repository.save(new IdempotencyRecord(rec.getId(), key, rec.getResourceType(),
                        resourceId, status, rec.getCreatedAt())));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void abandon(String key) {
        repository.findByKey(key).ifPresent(repository::delete);
    }

    @Scheduled(cron = "0 17 3 * * *")
    @Transactional
    public void purge() {
        repository.deleteByCreatedAtBefore(Instant.now(clock).minus(1, ChronoUnit.DAYS));
    }
}
