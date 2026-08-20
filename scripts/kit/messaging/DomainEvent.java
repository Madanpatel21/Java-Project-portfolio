package com.java700.kit.messaging;

import java.time.Instant;

public interface DomainEvent {

    String eventId();

    Instant occurredAt();
}
