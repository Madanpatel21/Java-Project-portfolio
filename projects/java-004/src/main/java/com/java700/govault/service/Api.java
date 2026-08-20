package com.java700.govault.service;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/** Request/response records for the document governance API. */
public final class Api {

    private Api() {
    }

    public record DocumentView(String id, String title, String fileName, String contentType,
                               long sizeBytes, String classification, String retentionClass,
                               String ownerName, String contentHash, String status,
                               boolean legalHold, Instant uploadedAt, Instant disposedAt) {
    }

    public record HoldView(String id, String name, String reason, String appliedBy,
                           Instant appliedAt, Instant releasedAt, String status) {
    }

    public record CreateHoldRequest(@NotBlank String name, @NotBlank String reason) {
    }

    public record ClassifyRequest(@NotBlank String classification, @NotBlank String retentionClass) {
    }

    public record ProofView(String documentId, String contentHash, String retentionClass,
                            Instant disposedAt, String executor, String disposition) {
    }

    public record ScanResult(int disposed, int protectedByHold) {
    }
}
