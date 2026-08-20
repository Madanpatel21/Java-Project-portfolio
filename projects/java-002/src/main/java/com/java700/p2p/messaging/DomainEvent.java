package com.java700.p2p.messaging;

import java.time.Instant;

public interface DomainEvent {

    String eventId();

    Instant occurredAt();
}
