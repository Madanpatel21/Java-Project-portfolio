package com.java700.roster.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Broker publisher with resilience4j retry; queues carry a DLX (docker/rabbitmq). */
@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class RabbitEventBus implements DomainEventBus {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventBus.class);
    public static final String EXCHANGE = "roster.events";

    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;

    public RabbitEventBus(RabbitTemplate rabbit, ObjectMapper mapper) {
        this.rabbit = rabbit;
        this.mapper = mapper;
    }

    @Override
    @Retry(name = "eventPublish", fallbackMethod = "publishFallback")
    public void publish(DomainEvent event) {
        try {
            rabbit.convertAndSend(EXCHANGE, event.getClass().getSimpleName(),
                    mapper.writeValueAsString(event));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize domain event", e);
        }
    }

    void publishFallback(DomainEvent event, Throwable t) {
        log.error("Failed to publish event {} after retries: {}", event.eventId(), t.getMessage());
    }
}
