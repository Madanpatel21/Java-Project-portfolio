package com.java700.stewardship.reviews;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class ReviewApi {

    private ReviewApi() {
    }

    public record TaskView(String id, String prescriptionId, String triggerReason, Instant dueAt,
                           String status, String assignedTo, String createdBy, Instant createdAt,
                           Instant completedAt) {

        static TaskView from(ReviewTask t) {
            return new TaskView(t.getId(), t.getPrescriptionId(), t.getTriggerReason(), t.getDueAt(),
                    t.getStatus().name(), t.getAssignedTo(), t.getCreatedBy(), t.getCreatedAt(),
                    t.getCompletedAt());
        }
    }

    public record AssignRequest(@NotBlank String pharmacist) {
    }
}
