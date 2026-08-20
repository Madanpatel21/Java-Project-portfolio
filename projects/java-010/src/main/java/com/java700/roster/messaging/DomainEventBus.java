package com.java700.roster.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
