package com.java700.expfraud.messaging;

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

/** Declares expense event queues (with dead-lettering) and consumes claim/case events. */
@Configuration
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class RabbitConsumers {

    public static final String DLX = "expfraud.dlx";
    public static final String QUEUE_CLAIMS = "expfraud.claims";
    public static final String QUEUE_CASES = "expfraud.cases";

    private final EventDispatcher dispatcher;

    public RabbitConsumers(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Bean
    DirectExchange expenseExchange() {
        return new DirectExchange(RabbitEventBus.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue claimsQueue() {
        return QueueBuilder.durable(QUEUE_CLAIMS)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_CLAIMS + ".dlq").build();
    }

    @Bean
    Queue casesQueue() {
        return QueueBuilder.durable(QUEUE_CASES)
                .deadLetterExchange(DLX).deadLetterRoutingKey(QUEUE_CASES + ".dlq").build();
    }

    @Bean
    Queue claimsDlq() {
        return QueueBuilder.durable(QUEUE_CLAIMS + ".dlq").build();
    }

    @Bean
    Queue casesDlq() {
        return QueueBuilder.durable(QUEUE_CASES + ".dlq").build();
    }

    @Bean
    Binding submittedBinding(Queue claimsQueue, DirectExchange expenseExchange) {
        return BindingBuilder.bind(claimsQueue).to(expenseExchange).with("ClaimSubmitted");
    }

    @Bean
    Binding scoredBinding(Queue claimsQueue, DirectExchange expenseExchange) {
        return BindingBuilder.bind(claimsQueue).to(expenseExchange).with("ClaimScored");
    }

    @Bean
    Binding openedBinding(Queue casesQueue, DirectExchange expenseExchange) {
        return BindingBuilder.bind(casesQueue).to(expenseExchange).with("CaseOpened");
    }

    @Bean
    Binding decidedBinding(Queue casesQueue, DirectExchange expenseExchange) {
        return BindingBuilder.bind(casesQueue).to(expenseExchange).with("CaseDecided");
    }

    @RabbitListener(queues = QUEUE_CLAIMS)
    void onClaimEvent(@Payload String json) {
        if (json.contains("\"claimNo\"")) {
            dispatcher.dispatch("ClaimSubmitted", json);
        } else {
            dispatcher.dispatch("ClaimScored", json);
        }
    }

    @RabbitListener(queues = QUEUE_CASES)
    void onCaseEvent(@Payload String json) {
        if (json.contains("\"decidedBy\"")) {
            dispatcher.dispatch("CaseDecided", json);
        } else {
            dispatcher.dispatch("CaseOpened", json);
        }
    }
}
