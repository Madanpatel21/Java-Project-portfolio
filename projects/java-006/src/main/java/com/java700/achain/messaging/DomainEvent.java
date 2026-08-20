package com.java700.achain.messaging;

import java.time.Instant;

public interface DomainEvent {

    String eventId();

    Instant occurredAt();
}
