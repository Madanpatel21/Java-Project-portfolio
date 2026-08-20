package com.java700.expfraud.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
