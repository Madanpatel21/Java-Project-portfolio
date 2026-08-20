package com.java700.p2p.messaging;

public interface DomainEventHandler<T extends DomainEvent> {

    Class<T> supportedType();

    void handle(T event);
}
