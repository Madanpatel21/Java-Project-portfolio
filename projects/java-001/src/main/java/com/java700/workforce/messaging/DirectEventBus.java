package com.java700.workforce.messaging;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** In-process dispatcher for dev/test (no broker): invokes handlers synchronously. */
@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "false", matchIfMissing = true)
public class DirectEventBus implements DomainEventBus {

    private static final Logger log = LoggerFactory.getLogger(DirectEventBus.class);

    private final org.springframework.beans.factory.ObjectProvider<List<DomainEventHandler<?>>> handlersProvider;

    /** Lazily resolved to avoid a handler -> service -> bus construction cycle. */
    public DirectEventBus(org.springframework.beans.factory.ObjectProvider<List<DomainEventHandler<?>>> handlersProvider) {
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
