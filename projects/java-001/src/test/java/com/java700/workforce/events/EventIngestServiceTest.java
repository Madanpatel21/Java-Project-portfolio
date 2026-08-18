package com.java700.workforce.events;

import com.java700.workforce.events.AccessEventRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.java700.workforce.common.api.Problems;
import com.java700.workforce.common.web.IdempotencyService;
import com.java700.workforce.events.AccessEventRepository;
import com.java700.workforce.events.EventIngestService.AccessEventIngested;
import com.java700.workforce.identity.UserProfileRepository;
import com.java700.workforce.messaging.DomainEventBus;
import com.java700.workforce.observability.Metrics;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventIngestServiceTest {

    private AccessEventRepository repository;
    private DomainEventBus bus;
    private IdempotencyService idempotency;
    private EventIngestService service;

    @BeforeEach
    void setUp() {
        repository = mock(AccessEventRepository.class);
        bus = mock(DomainEventBus.class);
        idempotency = mock(IdempotencyService.class);
        service = new EventIngestService(repository, mock(UserProfileRepository.class), bus,
                idempotency, mock(Metrics.class), Clock.systemUTC());
    }

    private EventApi.IngestRequest req() {
        return new EventApi.IngestRequest("u1", "payroll-app", "LOGIN", "10.0.0.1", "hr-system",
                "ext-1", Instant.now());
    }

    @Test
    void publishesAndRecordsIdempotencyKey() {
        when(idempotency.begin("k1", "ACCESS_EVENT")).thenReturn(null);
        EventApi.IngestResponse resp = service.ingest(req(), "k1");
        assertThat(resp.duplicate()).isFalse();
        verify(bus).publish(any(AccessEventIngested.class));
        verify(idempotency).complete(anyString(), anyString(), org.mockito.ArgumentMatchers.eq(202));
    }

    @Test
    void replayReturnsOriginalEventId() {
        when(idempotency.begin("k1", "ACCESS_EVENT")).thenReturn("evt-original");
        EventApi.IngestResponse resp = service.ingest(req(), "k1");
        assertThat(resp.duplicate()).isTrue();
        assertThat(resp.eventId()).isEqualTo("evt-original");
        verify(bus, never()).publish(any());
    }

    @Test
    void duplicateSourceExternalIdIsRejected() {
        when(idempotency.begin("k1", "ACCESS_EVENT")).thenReturn(null);
        when(repository.existsBySourceAndExternalId("hr-system", "ext-1")).thenReturn(true);
        assertThatThrownBy(() -> service.ingest(req(), "k1"))
                .isInstanceOf(Problems.Conflict.class);
        verify(idempotency).abandon("k1");
        verify(bus, never()).publish(any());
    }
}
