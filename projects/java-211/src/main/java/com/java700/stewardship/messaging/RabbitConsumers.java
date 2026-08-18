package com.java700.stewardship.messaging;

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

/** Declares stewardship queues (with dead-lettering) and consumes domain events. */
@Configuration
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class RabbitConsumers {

    public static final String DLX = "stewardship.dlx";
    public static final String QUEUE_RX = "prescription.events";
    public static final String QUEUE_CULTURES = "culture.events";
    public static final String QUEUE_AUTH = "restricted.auth.events";

    private final EventDispatcher dispatcher;

    public RabbitConsumers(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Bean
    DirectExchange stewardshipExchange() {
        return new DirectExchange(RabbitEventBus.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue rxQueue() {
        return QueueBuilder.durable(QUEUE_RX)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_RX + ".dlq").build();
    }

    @Bean
    Queue culturesQueue() {
        return QueueBuilder.durable(QUEUE_CULTURES)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_CULTURES + ".dlq").build();
    }

    @Bean
    Queue authQueue() {
        return QueueBuilder.durable(QUEUE_AUTH)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_AUTH + ".dlq").build();
    }

    @Bean
    Queue rxDlq() {
        return QueueBuilder.durable(QUEUE_RX + ".dlq").build();
    }

    @Bean
    Queue culturesDlq() {
        return QueueBuilder.durable(QUEUE_CULTURES + ".dlq").build();
    }

    @Bean
    Queue authDlq() {
        return QueueBuilder.durable(QUEUE_AUTH + ".dlq").build();
    }

    @Bean
    Binding rxBinding() {
        return BindingBuilder.bind(rxQueue()).to(stewardshipExchange()).with("PrescriptionCreated");
    }

    @Bean
    Binding culturesBinding() {
        return BindingBuilder.bind(culturesQueue()).to(stewardshipExchange()).with("CultureReported");
    }

    @Bean
    Binding authBinding() {
        return BindingBuilder.bind(authQueue()).to(stewardshipExchange()).with("RestrictedAuthApproved");
    }

    @RabbitListener(queues = QUEUE_RX)
    void onPrescription(@Payload String json) {
        dispatcher.dispatch("PrescriptionCreated", json);
    }

    @RabbitListener(queues = QUEUE_CULTURES)
    void onCulture(@Payload String json) {
        dispatcher.dispatch("CultureReported", json);
    }

    @RabbitListener(queues = QUEUE_AUTH)
    void onAuth(@Payload String json) {
        dispatcher.dispatch("RestrictedAuthApproved", json);
    }
}
