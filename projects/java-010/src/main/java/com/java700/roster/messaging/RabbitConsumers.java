package com.java700.roster.messaging;

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

/** Declares roster event queues (with dead-lettering) and consumes roster events. */
@Configuration
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class RabbitConsumers {

    public static final String DLX = "roster.dlx";
    public static final String QUEUE_ROSTERS = "roster.lifecycle";
    public static final String QUEUE_SWAPS = "roster.swaps";

    private final EventDispatcher dispatcher;

    public RabbitConsumers(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Bean
    DirectExchange rosterExchange() {
        return new DirectExchange(RabbitEventBus.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue rostersQueue() {
        return QueueBuilder.durable(QUEUE_ROSTERS)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_ROSTERS + ".dlq").build();
    }

    @Bean
    Queue swapsQueue() {
        return QueueBuilder.durable(QUEUE_SWAPS)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_SWAPS + ".dlq").build();
    }

    @Bean
    Queue rostersDlq() {
        return QueueBuilder.durable(QUEUE_ROSTERS + ".dlq").build();
    }

    @Bean
    Queue swapsDlq() {
        return QueueBuilder.durable(QUEUE_SWAPS + ".dlq").build();
    }

    @Bean
    Binding publishedBinding(Queue rostersQueue, DirectExchange rosterExchange) {
        return BindingBuilder.bind(rostersQueue).to(rosterExchange).with("RosterPublished");
    }

    @Bean
    Binding optimizedBinding(Queue rostersQueue, DirectExchange rosterExchange) {
        return BindingBuilder.bind(rostersQueue).to(rosterExchange).with("RosterOptimized");
    }

    @Bean
    Binding swapBinding(Queue swapsQueue, DirectExchange rosterExchange) {
        return BindingBuilder.bind(swapsQueue).to(rosterExchange).with("SwapApproved");
    }

    @RabbitListener(queues = QUEUE_ROSTERS)
    void onRosterEvent(@Payload String json) {
        if (json.contains("\"score\"")) {
            dispatcher.dispatch("RosterOptimized", json);
        } else {
            dispatcher.dispatch("RosterPublished", json);
        }
    }

    @RabbitListener(queues = QUEUE_SWAPS)
    void onSwapEvent(@Payload String json) {
        dispatcher.dispatch("SwapApproved", json);
    }
}
