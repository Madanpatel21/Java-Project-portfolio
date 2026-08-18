package com.java700.workforce.audit;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class ExportApi {

    private ExportApi() {
    }

    public record CreateExportRequest(String scopeUserId,
                                      @NotNull Instant rangeFrom,
                                      @NotNull Instant rangeTo) {
    }

    public record ExportJobView(String id, String requestedBy, String scopeUserId,
                                Instant rangeFrom, Instant rangeTo, String status,
                                Long startSeq, Long endSeq, String hmac,
                                String error, Instant createdAt, Instant completedAt) {

        static ExportJobView from(ExportJob j) {
            return new ExportJobView(j.getId(), j.getRequestedBy(), j.getScopeUserId(),
                    j.getRangeFrom(), j.getRangeTo(), j.getStatus().name(), j.getStartSeq(),
                    j.getEndSeq(), j.getHmac(), j.getError(), j.getCreatedAt(), j.getCompletedAt());
        }
    }

    public record VerifyResponse(boolean valid, String algorithm, Long startSeq, Long endSeq) {
    }
}
