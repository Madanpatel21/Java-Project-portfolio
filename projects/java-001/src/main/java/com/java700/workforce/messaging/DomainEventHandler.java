package com.java700.workforce.messaging;

/** Typed handler for a domain event; used by both the Rabbit and direct dispatchers. */
public interface DomainEventHandler<T extends DomainEvent> {

    Class<T> supportedType();

    void handle(T event);
}
