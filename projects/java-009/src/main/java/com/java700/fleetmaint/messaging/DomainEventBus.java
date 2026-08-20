package com.java700.fleetmaint.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
