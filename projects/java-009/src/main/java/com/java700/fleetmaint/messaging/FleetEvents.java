package com.java700.fleetmaint.messaging;

import java.time.Instant;

/** Domain events published on the fleet event bus. */
public final class FleetEvents {

    private FleetEvents() {
    }

    public record ServiceDue(String eventId, Instant occurredAt, String taskId, String taskNo,
                             String vehicleId, String plate, String planCode,
                             boolean overdue) implements DomainEvent {
    }

    public record TamperFlagged(String eventId, Instant occurredAt, String vehicleId,
                                String plate, int reading) implements DomainEvent {
    }

    public record WorkOrderCompleted(String eventId, Instant occurredAt, String workOrderId,
                                     String woNo, String vehicleId, String plate)
            implements DomainEvent {
    }

    public record InspectionFailed(String eventId, Instant occurredAt, String vehicleId,
                                   String plate, String inspectionType) implements DomainEvent {
    }
}
