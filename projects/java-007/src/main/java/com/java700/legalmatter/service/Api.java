package com.java700.legalmatter.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Request/response records for the legal-matter API. */
public final class Api {

    private Api() {
    }

    public record CreatePartyRequest(@NotBlank String name, @NotBlank String type) {
    }

    public record CreateMatterRequest(@NotBlank String matterNo, @NotBlank String name,
                                      @NotBlank String clientPartyId, String practiceArea) {
    }

    public record AddMatterPartyRequest(@NotBlank String partyId, @NotBlank String role) {
    }

    public record ScreenRequest(@NotBlank String subjectName,
                                @NotNull List<@NotBlank String> adverseNames) {

        public ScreenRequest {
            adverseNames = List.copyOf(adverseNames);
        }
    }

    public record ScreenView(String id, String requestedBy, Instant checkedAt, String subjectName,
                             String adverseNames, String result, String detailsJson) {
    }

    public record ComputeDeadlinesRequest(@NotBlank String jurisdiction,
                                          @NotNull LocalDate triggerDate) {
    }

    public record DeadlineView(String id, String matterId, String eventType, String jurisdiction,
                               LocalDate dueAt, String status, String completedBy) {
    }

    public record PartyView(String id, String name, String type) {
    }

    public record MatterView(String id, String matterNo, String name, String status,
                             String clientPartyId, String practiceArea, Instant openedAt,
                             Instant closedAt) {
    }
}
