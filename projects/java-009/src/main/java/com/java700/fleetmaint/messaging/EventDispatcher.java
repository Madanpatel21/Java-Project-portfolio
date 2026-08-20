package com.java700.fleetmaint.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Decodes broker messages and routes them to typed handlers (lazily resolved). */
@Component
public class EventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EventDispatcher.class);

    private final Map<String, DomainEventHandler<?>> byType = new HashMap<>();
    private final ObjectMapper mapper;

    public EventDispatcher(ObjectMapper mapper, ObjectProvider<List<DomainEventHandler<?>>> handlersProvider) {
        this.mapper = mapper;
        handlersProvider.getIfAvailable(java.util.List::of).forEach(h -> byType.put(h.supportedType().getSimpleName(), h));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void dispatch(String typeName, String json) {
        DomainEventHandler handler = byType.get(typeName);
        if (handler == null) {
            log.warn("No handler registered for event type {}", typeName);
            return;
        }
        try {
            Object event = mapper.readValue(json, handler.supportedType());
            ((DomainEventHandler) handler).handle((DomainEvent) event);
        } catch (Exception e) {
            log.error("Dispatch failed for {}: {}", typeName, e.getMessage(), e);
            throw new IllegalStateException("Event dispatch failed", e);
        }
    }
}
