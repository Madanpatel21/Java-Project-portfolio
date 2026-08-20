package com.java700.legalmatter.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
