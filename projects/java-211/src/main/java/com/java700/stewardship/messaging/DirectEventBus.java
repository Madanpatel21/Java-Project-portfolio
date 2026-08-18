package com.java700.stewardship.messaging;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** In-process dispatcher for dev/test (no broker). */
@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "false", matchIfMissing = true)
public class DirectEventBus implements DomainEventBus {

    private static final Logger log = LoggerFactory.getLogger(DirectEventBus.class);

    private final ObjectProvider<List<DomainEventHandler<?>>> handlersProvider;

    public DirectEventBus(ObjectProvider<List<DomainEventHandler<?>>> handlersProvider) {
        this.handlersProvider = handlersProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void publish(DomainEvent event) {
        for (DomainEventHandler handler : handlersProvider.getObject()) {
            if (handler.supportedType().isInstance(event)) {
                try {
                    handler.handle(event);
                } catch (RuntimeException e) {
                    log.error("Handler {} failed for event {}: {}", handler.getClass().getSimpleName(),
                            event.eventId(), e.getMessage(), e);
                    throw e;
                }
            }
        }
    }
}
