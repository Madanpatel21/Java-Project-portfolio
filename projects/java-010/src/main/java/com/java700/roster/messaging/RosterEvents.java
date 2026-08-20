package com.java700.roster.messaging;

import java.time.Instant;

/** Domain events published on the roster event bus. */
public final class RosterEvents {

    private RosterEvents() {
    }

    public record RosterPublished(String eventId, Instant occurredAt, String rosterId,
                                  String name) implements DomainEvent {
    }

    public record RosterOptimized(String eventId, Instant occurredAt, String rosterId,
                                  String score) implements DomainEvent {
    }

    public record SwapApproved(String eventId, Instant occurredAt, String swapNo,
                               String shiftId, String newEmployeeEmpNo) implements DomainEvent {
    }
}
