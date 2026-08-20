package com.java700.roster.messaging;

public interface DomainEventHandler<T extends DomainEvent> {

    Class<T> supportedType();

    void handle(T event);
}
