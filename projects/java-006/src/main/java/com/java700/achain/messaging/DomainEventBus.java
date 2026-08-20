package com.java700.achain.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
