package com.java700.achain.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

/** Request/response records for the approval API. */
public final class Api {

    private Api() {
    }

    public record CreatePolicyRequest(@NotBlank String policyCode, @NotBlank String name,
                                      String description, @NotBlank String rulesJson) {
    }

    public record CreateChainRequest(@NotBlank String chainCode, @NotBlank String name,
                                     @NotBlank String stepsJson) {
    }

    public record CreateRequestRequest(@NotBlank String chainCode, @NotBlank String policyCode,
                                       @NotBlank String subjectType, @NotBlank String subjectId,
                                       Map<String, Object> payload, @NotNull Instant dueAt) {

        public CreateRequestRequest {
            payload = payload == null ? Map.of() : Map.copyOf(payload);
        }
    }

    public record DecideRequest(String note) {
    }

    public record PolicyView(String id, String policyCode, String name, String activeVersionId) {
    }

    public record ChainView(String id, String chainCode, String name, String stepsJson) {
    }

    public record RequestView(String id, String chainId, String policyVersionId, String subjectType,
                              String subjectId, String status, int currentStep,
                              String requestedByName, Instant dueAt, Instant createdAt,
                              Instant decidedAt) {
    }

    public record DecisionView(String id, String requestId, int stepNo, String approverName,
                               String decision, String note, Instant decidedAt) {
    }

    public record EscalationResult(int escalated) {
    }
}
