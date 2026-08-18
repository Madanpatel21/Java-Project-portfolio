package com.java700.crvs.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public final class RegistrationApi {

    private RegistrationApi() {
    }

    public record BirthRequest(@NotBlank String fullName, @NotNull LocalDate dob,
                               @NotBlank String sex, @NotBlank String placeOfBirth,
                               String parentNames) {
    }

    public record MarriageRequest(@NotBlank String personId, @NotBlank String spousePersonId) {
    }

    public record DeathRequest(@NotBlank String personId, @NotBlank String cause) {
    }

    public record CorrectionRequest(@NotBlank String personId, @NotBlank String field,
                                    @NotBlank String newValue, String reason) {
    }

    public record CreateResponse(String id, String status) {
    }

    public record DecideRequest(String note) {
    }

    public record RegistrationView(String id, String type, String personId, String spousePersonId,
                                   Map<String, Object> payload, String status, String officeId,
                                   String registrarName, String supervisorName,
                                   Instant decidedAt, String decisionNote, Instant createdAt) {

        public RegistrationView {
            payload = payload == null ? Map.of() : Map.copyOf(payload);
        }

        static RegistrationView from(Registration r, Map<String, Object> payload) {
            return new RegistrationView(r.getId(), r.getType().name(), r.getPersonId(),
                    r.getSpousePersonId(), payload, r.getStatus().name(), r.getOfficeId(),
                    r.getRegistrarName(), r.getSupervisorName(), r.getDecidedAt(),
                    r.getDecisionNote(), r.getCreatedAt());
        }
    }
}
