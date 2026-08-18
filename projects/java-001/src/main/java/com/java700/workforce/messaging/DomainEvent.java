package com.java700.workforce.messaging;

import java.time.Instant;

/** Marker for domain events flowing over the bus. */
public interface DomainEvent {

    String eventId();

    Instant occurredAt();
}
