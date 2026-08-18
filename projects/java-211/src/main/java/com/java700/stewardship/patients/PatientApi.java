package com.java700.stewardship.patients;

import com.java700.stewardship.common.masking.Masked;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class PatientApi {

    private PatientApi() {
    }

    public record PatientView(String id, @Masked String mrn, @Masked String name, LocalDate dob,
                              String sex, BigDecimal weightKg) {

        static PatientView from(Patient p) {
            return new PatientView(p.getId(), p.getMrn(), p.getName(), p.getDob(), p.getSex(),
                    p.getWeightKg());
        }
    }

    public record CreatePatientRequest(@NotBlank String mrn, @NotBlank String name,
                                       @NotNull LocalDate dob, @NotBlank String sex,
                                       @NotNull @Positive BigDecimal weightKg) {
    }

    public record AdmissionView(String id, String patientId, String ward, Instant admittedAt,
                                Instant dischargedAt) {

        static AdmissionView from(Admission a) {
            return new AdmissionView(a.getId(), a.getPatientId(), a.getWard(), a.getAdmittedAt(),
                    a.getDischargedAt());
        }
    }

    public record CreateAdmissionRequest(@NotBlank String patientId, @NotBlank String ward,
                                         @NotNull Instant admittedAt, Instant dischargedAt) {
    }

    public record CreateLabValueRequest(@NotBlank String patientId, @NotBlank String type,
                                        @NotNull BigDecimal value, @NotBlank String unit,
                                        @NotNull Instant measuredAt) {
    }
}
