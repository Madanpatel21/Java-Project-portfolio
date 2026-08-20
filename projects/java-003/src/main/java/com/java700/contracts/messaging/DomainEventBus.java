package com.java700.contracts.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
