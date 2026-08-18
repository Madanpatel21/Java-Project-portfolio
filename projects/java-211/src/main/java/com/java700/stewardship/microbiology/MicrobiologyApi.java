package com.java700.stewardship.microbiology;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class MicrobiologyApi {

    private MicrobiologyApi() {
    }

    public record CreateCultureRequest(@NotBlank String patientId, @NotBlank String specimenType,
                                       @NotNull Instant collectedAt) {
    }

    public record AddIsolateRequest(@NotBlank String organism,
                                    @NotEmpty List<SusceptibilityEntry> susceptibility) {

        public AddIsolateRequest {
            susceptibility = List.copyOf(susceptibility);
        }
    }

    public record SusceptibilityEntry(@NotBlank String drugCode, @NotBlank String result,
                                      BigDecimal micValue) {
    }

    public record CultureView(String id, String patientId, String specimenType,
                              Instant collectedAt, Instant reportedAt,
                              List<IsolateView> isolates) {

        public CultureView {
            isolates = List.copyOf(isolates);
        }
    }

    public record IsolateView(String id, String organism, Instant collectedAt,
                              List<SuscView> susceptibility) {

        public IsolateView {
            susceptibility = List.copyOf(susceptibility);
        }
    }

    public record SuscView(String drugCode, String result, BigDecimal micValue) {
    }
}
