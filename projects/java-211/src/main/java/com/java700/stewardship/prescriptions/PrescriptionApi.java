package com.java700.stewardship.prescriptions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public final class PrescriptionApi {

    private PrescriptionApi() {
    }

    public record CreateRequest(@NotBlank String patientId, @NotBlank String admissionId,
                                @NotBlank String drugCode, @NotBlank String indication,
                                @NotBlank String route, @NotNull @Positive BigDecimal doseMg,
                                @Positive int frequencyHours, @NotNull Instant startAt,
                                boolean empiric) {
    }

    public record RxView(String id, String patientId, String admissionId, String drugId,
                         String drugCode, String indication, String route, BigDecimal doseMg,
                         int frequencyHours, Instant startAt, Instant stopAt, String status,
                         boolean empiric, String prescribedBy, String restrictedAuthId) {

        static RxView from(Prescription p, String drugCode) {
            return new RxView(p.getId(), p.getPatientId(), p.getAdmissionId(), p.getDrugId(),
                    drugCode, p.getIndication(), p.getRoute(), p.getDoseMg(), p.getFrequencyHours(),
                    p.getStartAt(), p.getStopAt(), p.getStatus().name(), p.isEmpiric(),
                    p.getPrescribedBy(), p.getRestrictedAuthId());
        }
    }

    public record CreateResponse(String id, String status, boolean preAuthorizationRequired) {
    }
}
