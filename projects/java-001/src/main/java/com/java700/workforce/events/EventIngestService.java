package com.java700.workforce.events;


import com.java700.workforce.common.api.Problems;
import com.java700.workforce.common.web.IdempotencyService;
import com.java700.workforce.identity.UserProfileRepository;
import com.java700.workforce.messaging.DomainEvent;
import com.java700.workforce.messaging.DomainEventBus;
import com.java700.workforce.messaging.DomainEventHandler;
import com.java700.workforce.observability.Metrics;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ingestion of access events. Endpoint publishes to the bus (async in the local profile);
 * the persister below is the single writer, idempotent on (source, externalId).
 */
@Service
public class EventIngestService {

    private static final Logger log = LoggerFactory.getLogger(EventIngestService.class);

    private final AccessEventRepository repository;
    private final UserProfileRepository userRepository;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final Metrics metrics;
    private final Clock clock;

    public EventIngestService(AccessEventRepository repository, UserProfileRepository userRepository,
                              DomainEventBus bus, IdempotencyService idempotency, Metrics metrics,
                              Clock clock) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.bus = bus;
        this.idempotency = idempotency;
        this.metrics = metrics;
        this.clock = clock;
    }

    public EventApi.IngestResponse ingest(EventApi.IngestRequest req, String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "ACCESS_EVENT");
        if (existing != null) {
            return new EventApi.IngestResponse(existing, true);
        }
        if (req.externalId() != null
                && repository.existsBySourceAndExternalId(req.source(), req.externalId())) {
            idempotency.abandon(idempotencyKey);
            throw new Problems.Conflict("Duplicate access event (source + externalId already ingested)");
        }
        String eventId = UUID.randomUUID().toString();
        try {
            bus.publish(new AccessEventIngested(eventId, Instant.now(clock), eventId, req.userId(),
                    req.resourceName(), req.eventType(), req.ipAddress(), req.source(),
                    req.externalId(), req.occurredAt() == null ? Instant.now(clock) : req.occurredAt()));
        } catch (RuntimeException e) {
            idempotency.abandon(idempotencyKey);
            throw e;
        }
        idempotency.complete(idempotencyKey, eventId, 202);
        return new EventApi.IngestResponse(eventId, false);
    }

    /** Single writer: persists ingested events (idempotent on source+externalId). */
    @Component
    public static class EventPersister implements DomainEventHandler<AccessEventIngested> {

        private final AccessEventRepository repository;
        private final UserProfileRepository userRepository;
        private final Metrics metrics;

        public EventPersister(AccessEventRepository repository, UserProfileRepository userRepository,
                              Metrics metrics) {
            this.repository = repository;
            this.userRepository = userRepository;
            this.metrics = metrics;
        }

        @Override
        public Class<AccessEventIngested> supportedType() {
            return AccessEventIngested.class;
        }

        @Override
        @Transactional
        public void handle(AccessEventIngested e) {
            if (e.externalId() != null && repository.existsBySourceAndExternalId(e.source(), e.externalId())) {
                log.debug("Duplicate event ignored: source={} externalId={}", e.source(), e.externalId());
                return;
            }
            if (userRepository.findById(e.userId()).isEmpty()) {
                log.warn("Access event for unknown user {} dropped", e.userId());
                return;
            }
            repository.save(new AccessEvent(e.eventId(), e.userId(), e.resourceName(), e.eventType(),
                    e.ipAddress(), e.source(), e.externalId(), e.occurredAt()));
            metrics.incrementIngestedEvents();
        }
    }

    public record AccessEventIngested(String eventId, Instant occurredAt, String ingestedEventId,
                                      String userId, String resourceName, String eventType,
                                      String ipAddress, String source, String externalId,
                                      Instant eventOccurredAt) implements DomainEvent {
    }
}
