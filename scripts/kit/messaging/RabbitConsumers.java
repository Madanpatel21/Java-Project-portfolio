package com.java700.kit.messaging;

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

/** Declares CRVS queues (with dead-lettering) and consumes registry events. */
@Configuration
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class RabbitConsumers {

    public static final String DLX = "crvs.dlx";
    public static final String QUEUE_PERSONS = "registry.persons";
    public static final String QUEUE_DEATHS = "registry.deaths";

    private final EventDispatcher dispatcher;

    public RabbitConsumers(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Bean
    DirectExchange crvsExchange() {
        return new DirectExchange(RabbitEventBus.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue personsQueue() {
        return QueueBuilder.durable(QUEUE_PERSONS)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_PERSONS + ".dlq").build();
    }

    @Bean
    Queue deathsQueue() {
        return QueueBuilder.durable(QUEUE_DEATHS)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_DEATHS + ".dlq").build();
    }

    @Bean
    Queue personsDlq() {
        return QueueBuilder.durable(QUEUE_PERSONS + ".dlq").build();
    }

    @Bean
    Queue deathsDlq() {
        return QueueBuilder.durable(QUEUE_DEATHS + ".dlq").build();
    }

    @Bean
    Binding personsBinding() {
        return BindingBuilder.bind(personsQueue()).to(crvsExchange()).with("PersonRegistered");
    }

    @Bean
    Binding deathsBinding() {
        return BindingBuilder.bind(deathsQueue()).to(crvsExchange()).with("DeathRegistered");
    }

    @RabbitListener(queues = QUEUE_PERSONS)
    void onPerson(@Payload String json) {
        dispatcher.dispatch("PersonRegistered", json);
    }

    @RabbitListener(queues = QUEUE_DEATHS)
    void onDeath(@Payload String json) {
        dispatcher.dispatch("DeathRegistered", json);
    }
}
