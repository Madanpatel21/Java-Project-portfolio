package com.java700.kit.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
