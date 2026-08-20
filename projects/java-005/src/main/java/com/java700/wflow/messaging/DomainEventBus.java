package com.java700.wflow.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
