package com.java700.p2p.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class Api {

    private Api() {
    }

    public record CreatePoRequest(@NotBlank String poNumber, @NotBlank String supplierId,
                                  @NotBlank String supplierName, @NotBlank String currency,
                                  @NotEmpty List<Line> lines) {

        public CreatePoRequest {
            lines = List.copyOf(lines);
        }

        public record Line(@NotBlank String itemCode, @NotBlank String description,
                           @NotNull BigDecimal quantity, @NotNull BigDecimal unitPrice) {
        }
    }

    public record CreateGrRequest(@NotBlank String grNumber, @NotBlank String poId,
                                  @NotEmpty List<GrLine> lines) {

        public CreateGrRequest {
            lines = List.copyOf(lines);
        }

        public record GrLine(@NotBlank String poLineId, @NotNull BigDecimal quantityReceived) {
        }
    }

    public record CreateInvoiceRequest(@NotBlank String invoiceNumber, @NotBlank String supplierId,
                                       @NotBlank String supplierName, @NotBlank String currency,
                                       @NotNull BigDecimal totalAmount, @NotNull LocalDate invoiceDate,
                                       LocalDate dueDate, @NotEmpty List<InvLine> lines) {

        public CreateInvoiceRequest {
            lines = List.copyOf(lines);
        }

        public record InvLine(@NotBlank String itemCode, @NotNull BigDecimal quantity,
                              @NotNull BigDecimal unitPrice, @NotNull BigDecimal lineTotal) {
        }
    }

    public record PoView(String id, String poNumber, String supplierId, String supplierName,
                        String currency, String status, java.time.Instant issuedAt,
                        List<PoLineView> lines) {

        public PoView {
            lines = List.copyOf(lines);
        }
    }

    public record PoLineView(String id, int lineNo, String itemCode, String description,
                             java.math.BigDecimal quantity, java.math.BigDecimal unitPrice,
                             java.math.BigDecimal receivedQty, java.math.BigDecimal invoicedQty) {
    }

    public record InvoiceView(String id, String invoiceNumber, String supplierName,
                              String currency, BigDecimal totalAmount, String status,
                              LocalDate invoiceDate, Instant createdAt) {
    }

    public record ExceptionView(String id, String invoiceId, String type, String severity,
                                String detail, String status, String assignedTo,
                                Instant createdAt, Instant resolvedAt, String resolvedBy,
                                String resolutionNote) {
    }

    public record DecideRequest(String note) {
    }

    public record AssignRequest(@NotBlank String assignee) {
    }

    public record BatchView(String id, Instant startedAt, Instant completedAt,
                            int invoicesProcessed, int exceptionsCreated,
                            int postingsCreated, String status) {
    }

    public record RuleView(String id, String ruleType, BigDecimal tolerancePct, String action,
                           boolean active) {
    }

    public record UpdateRuleRequest(@NotNull BigDecimal tolerancePct, @NotBlank String action) {
    }
}
