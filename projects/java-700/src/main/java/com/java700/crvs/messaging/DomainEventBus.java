package com.java700.crvs.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
