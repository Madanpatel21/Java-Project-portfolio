package com.java700.p2p.messaging;

public interface DomainEventBus {

    void publish(DomainEvent event);
}
