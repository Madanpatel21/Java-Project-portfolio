package com.java700.achain.messaging;

public interface DomainEventHandler<T extends DomainEvent> {

    Class<T> supportedType();

    void handle(T event);
}
