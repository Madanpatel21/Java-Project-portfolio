package com.java700.stewardship.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
