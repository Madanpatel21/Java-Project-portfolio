package com.java700.roster.messaging;

import java.time.Instant;

public interface DomainEvent {

    String eventId();

    Instant occurredAt();
}
