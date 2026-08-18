package com.java700.workforce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * Declares the workforce queues (with dead-lettering) and consumes domain events,
 * delegating to the same handlers the direct bus uses. Idempotent by design:
 * handlers tolerate duplicate delivery (verified via event-id dedup tables).
 */
@Configuration
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class RabbitConsumers {

    private static final Logger log = LoggerFactory.getLogger(RabbitConsumers.class);
    public static final String DLX = "workforce.dlx";
    public static final String QUEUE_ACCESS_EVENTS = "access.events";
    public static final String QUEUE_VIOLATIONS = "compliance.violations";
    public static final String QUEUE_AUDIT_EXPORTS = "audit.exports";

    private final EventDispatcher dispatcher;

    public RabbitConsumers(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Bean
    DirectExchange workforceExchange() {
        return new DirectExchange(RabbitEventBus.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue accessEventsQueue() {
        return QueueBuilder.durable(QUEUE_ACCESS_EVENTS)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_ACCESS_EVENTS + ".dlq").build();
    }

    @Bean
    Queue violationsQueue() {
        return QueueBuilder.durable(QUEUE_VIOLATIONS)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_VIOLATIONS + ".dlq").build();
    }

    @Bean
    Queue auditExportsQueue() {
        return QueueBuilder.durable(QUEUE_AUDIT_EXPORTS)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_AUDIT_EXPORTS + ".dlq").build();
    }

    @Bean
    Queue accessEventsDlq() {
        return QueueBuilder.durable(QUEUE_ACCESS_EVENTS + ".dlq").build();
    }

    @Bean
    Queue violationsDlq() {
        return QueueBuilder.durable(QUEUE_VIOLATIONS + ".dlq").build();
    }

    @Bean
    Queue auditExportsDlq() {
        return QueueBuilder.durable(QUEUE_AUDIT_EXPORTS + ".dlq").build();
    }

    @Bean
    Binding accessEventsBinding() {
        return BindingBuilder.bind(accessEventsQueue()).to(workforceExchange())
                .with("AccessEventIngested");
    }

    @Bean
    Binding violationsBinding() {
        return BindingBuilder.bind(violationsQueue()).to(workforceExchange())
                .with("ViolationDetected");
    }

    @Bean
    Binding auditExportsBinding() {
        return BindingBuilder.bind(auditExportsQueue()).to(workforceExchange())
                .with("ExportRequested");
    }

    @RabbitListener(queues = QUEUE_ACCESS_EVENTS)
    void onAccessEvent(@Payload String json) {
        dispatcher.dispatch("AccessEventIngested", json);
    }

    @RabbitListener(queues = QUEUE_VIOLATIONS)
    void onViolation(@Payload String json) {
        dispatcher.dispatch("ViolationDetected", json);
    }

    @RabbitListener(queues = QUEUE_AUDIT_EXPORTS)
    void onExport(@Payload String json) {
        dispatcher.dispatch("ExportRequested", json);
    }
}
