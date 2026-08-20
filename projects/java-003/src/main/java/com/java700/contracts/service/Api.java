package com.java700.contracts.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.time.LocalDate;

/** Request/response records for the contract & obligation API. */
public final class Api {

    private Api() {
    }

    public record CreateContractRequest(@NotBlank String contractNo, @NotBlank String title,
                                        @NotBlank String counterparty, LocalDate effectiveFrom,
                                        LocalDate effectiveTo) {
    }

    public record CreateVersionRequest(@NotBlank String contentJson) {
    }

    public record CreateObligationRequest(@NotBlank String contractId, String sourceClause,
                                          @NotBlank String type, @NotBlank String title,
                                          String description, @NotNull Instant dueAt,
                                          @Positive int windowBeforeDays,
                                          @Positive Integer repeatIntervalDays,
                                          @NotBlank String criticality, String assignedTo) {
    }

    public record DecideRequest(String note) {
    }

    public record ContractView(String id, String contractNo, String title, String counterparty,
                               String ownerName, String status, LocalDate effectiveFrom,
                               LocalDate effectiveTo, Instant createdAt) {
    }

    public record VersionView(String id, String contractId, int versionNo, String contentJson,
                              String createdBy, Instant createdAt) {
    }

    public record ObligationView(String id, String contractId, String sourceClause, String type,
                                 String title, String description, Instant dueAt,
                                 int windowBeforeDays, Integer repeatIntervalDays,
                                 String criticality, String status, String assignedTo,
                                 Instant acknowledgedAt, Instant completedAt, Instant waivedAt,
                                 String waivedBy, String waiverReason, Instant notifiedAt,
                                 Instant overdueAt, Instant createdAt) {
    }

    public record ScanResult(int notified, int overdue) {
    }
}
