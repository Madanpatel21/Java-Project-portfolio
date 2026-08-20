package com.java700.fleetmaint.messaging;

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

/** Declares fleet event queues (with dead-lettering) and consumes service events. */
@Configuration
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class RabbitConsumers {

    public static final String DLX = "fleet.dlx";
    public static final String QUEUE_DUE = "fleet.service-due";
    public static final String QUEUE_FLAGS = "fleet.flags";

    private final EventDispatcher dispatcher;

    public RabbitConsumers(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Bean
    DirectExchange fleetExchange() {
        return new DirectExchange(RabbitEventBus.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue dueQueue() {
        return QueueBuilder.durable(QUEUE_DUE)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_DUE + ".dlq").build();
    }

    @Bean
    Queue flagsQueue() {
        return QueueBuilder.durable(QUEUE_FLAGS)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_FLAGS + ".dlq").build();
    }

    @Bean
    Queue dueDlq() {
        return QueueBuilder.durable(QUEUE_DUE + ".dlq").build();
    }

    @Bean
    Queue flagsDlq() {
        return QueueBuilder.durable(QUEUE_FLAGS + ".dlq").build();
    }

    @Bean
    Binding dueBinding(Queue dueQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(dueQueue).to(fleetExchange).with("ServiceDue");
    }

    @Bean
    Binding completedBinding(Queue dueQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(dueQueue).to(fleetExchange).with("WorkOrderCompleted");
    }

    @Bean
    Binding tamperBinding(Queue flagsQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(flagsQueue).to(fleetExchange).with("TamperFlagged");
    }

    @Bean
    Binding inspectionBinding(Queue flagsQueue, DirectExchange fleetExchange) {
        return BindingBuilder.bind(flagsQueue).to(fleetExchange).with("InspectionFailed");
    }

    @RabbitListener(queues = QUEUE_DUE)
    void onDueEvent(@Payload String json) {
        if (json.contains("\"woNo\"")) {
            dispatcher.dispatch("WorkOrderCompleted", json);
        } else {
            dispatcher.dispatch("ServiceDue", json);
        }
    }

    @RabbitListener(queues = QUEUE_FLAGS)
    void onFlagEvent(@Payload String json) {
        if (json.contains("\"inspectionType\"")) {
            dispatcher.dispatch("InspectionFailed", json);
        } else {
            dispatcher.dispatch("TamperFlagged", json);
        }
    }
}
