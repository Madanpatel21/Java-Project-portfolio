package com.java700.stewardship.interventions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;

public final class InterventionApi {

    private InterventionApi() {
    }

    public record ProposeRequest(@NotBlank String prescriptionId, String reviewTaskId,
                                 @NotBlank String type, Map<String, Object> detail,
                                 @NotBlank @Size(max = 2000) String reason) {

        public ProposeRequest {
            detail = detail == null ? Map.of() : Map.copyOf(detail);
        }
    }

    public record DecideRequest(@Size(max = 1000) String response) {
    }

    public record InterventionView(String id, String prescriptionId, String reviewTaskId,
                                   String type, Map<String, Object> detail, String reason,
                                   String status, String proposedBy, Instant proposedAt,
                                   String decidedBy, Instant decidedAt, String prescriberResponse) {

        public InterventionView {
            detail = detail == null ? Map.of() : Map.copyOf(detail);
        }

        static InterventionView from(Intervention i, Map<String, Object> detail) {
            return new InterventionView(i.getId(), i.getPrescriptionId(), i.getReviewTaskId(),
                    i.getType(), detail, i.getReason(), i.getStatus().name(), i.getProposedBy(),
                    i.getProposedAt(), i.getDecidedBy(), i.getDecidedAt(), i.getPrescriberResponse());
        }
    }
}
