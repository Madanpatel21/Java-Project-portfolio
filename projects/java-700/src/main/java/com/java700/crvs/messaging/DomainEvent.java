package com.java700.crvs.messaging;

import java.time.Instant;

public interface DomainEvent {

    String eventId();

    Instant occurredAt();
}
