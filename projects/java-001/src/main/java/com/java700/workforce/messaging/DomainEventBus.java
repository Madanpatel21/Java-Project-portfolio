package com.java700.workforce.messaging;

/** Publishes domain events. Implementations: RabbitMQ (local profile) or in-process (dev/test). */
public interface DomainEventBus {

    void publish(DomainEvent event);
}
