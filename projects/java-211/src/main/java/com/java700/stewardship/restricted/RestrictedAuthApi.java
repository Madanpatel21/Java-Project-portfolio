package com.java700.stewardship.restricted;

import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class RestrictedAuthApi {

    private RestrictedAuthApi() {
    }

    public record AuthView(String id, String prescriptionId, String requestedBy,
                           Instant requestedAt, String approvedBy, Instant approvedAt,
                           Instant expiresAt, String status, String reason) {

        static AuthView from(RestrictedAuthorization a) {
            return new AuthView(a.getId(), a.getPrescriptionId(), a.getRequestedBy(),
                    a.getRequestedAt(), a.getApprovedBy(), a.getApprovedAt(), a.getExpiresAt(),
                    a.getStatus().name(), a.getReason());
        }
    }

    public record DecideRequest(@Size(max = 1000) String note) {
    }
}
