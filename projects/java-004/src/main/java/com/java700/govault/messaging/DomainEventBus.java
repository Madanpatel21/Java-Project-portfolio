package com.java700.govault.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
