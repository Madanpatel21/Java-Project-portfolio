package com.java700.p2p.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.p2p.api.Api;
import com.java700.p2p.common.api.PageResponse;
import com.java700.p2p.common.api.Problems;
import com.java700.p2p.common.audit.AuditLogService;
import com.java700.p2p.common.web.IdempotencyService;
import com.java700.p2p.domain.BatchRun;
import com.java700.p2p.domain.BatchRunRepository;
import com.java700.p2p.domain.ExceptionRepository;
import com.java700.p2p.domain.GlPosting;
import com.java700.p2p.domain.GlPostingRepository;
import com.java700.p2p.domain.GoodsReceipt;
import com.java700.p2p.domain.GoodsReceiptLine;
import com.java700.p2p.domain.GoodsReceiptLineRepository;
import com.java700.p2p.domain.GoodsReceiptRepository;
import com.java700.p2p.domain.Invoice;
import com.java700.p2p.domain.InvoiceLine;
import com.java700.p2p.domain.InvoiceLineRepository;
import com.java700.p2p.domain.InvoiceRepository;
import com.java700.p2p.domain.MatchException;
import com.java700.p2p.domain.MatchException.Severity;
import com.java700.p2p.domain.MatchException.Status;
import com.java700.p2p.domain.OutboxRecord;
import com.java700.p2p.domain.OutboxRepository;
import com.java700.p2p.domain.PurchaseOrder;
import com.java700.p2p.domain.PurchaseOrderLine;
import com.java700.p2p.domain.PurchaseOrderLineRepository;
import com.java700.p2p.domain.PurchaseOrderRepository;
import com.java700.p2p.domain.ToleranceRule;
import com.java700.p2p.domain.ToleranceRuleRepository;
import com.java700.p2p.matching.MatchingEngine;
import com.java700.p2p.messaging.DomainEvent;
import com.java700.p2p.messaging.DomainEventBus;
import com.java700.p2p.observability.Metrics;
import com.java700.p2p.security.Roles;
import com.java700.p2p.security.SecurityUtil;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The P2P reconciliation core: ingest (PO/GR/invoice), three-way match, exception
 * resolution with four-eyes waivers, and the posting batch with transactional outbox.
 */
@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);
    public static final String ACCOUNT_GRNI = "2000-GRNI";
    public static final String ACCOUNT_AP = "2100-AP";

    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderLineRepository poLineRepository;
    private final GoodsReceiptRepository grRepository;
    private final GoodsReceiptLineRepository grLineRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final ToleranceRuleRepository ruleRepository;
    private final ExceptionRepository exceptionRepository;
    private final GlPostingRepository postingRepository;
    private final BatchRunRepository batchRepository;
    private final OutboxRepository outboxRepository;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final ObjectMapper mapper;
    private final Clock clock;

    public ReconciliationService(PurchaseOrderRepository poRepository,
                                 PurchaseOrderLineRepository poLineRepository,
                                 GoodsReceiptRepository grRepository,
                                 GoodsReceiptLineRepository grLineRepository,
                                 InvoiceRepository invoiceRepository,
                                 InvoiceLineRepository invoiceLineRepository,
                                 ToleranceRuleRepository ruleRepository,
                                 ExceptionRepository exceptionRepository,
                                 GlPostingRepository postingRepository,
                                 BatchRunRepository batchRepository,
                                 OutboxRepository outboxRepository,
                                 DomainEventBus bus, IdempotencyService idempotency,
                                 AuditLogService audit, Metrics metrics,
                                 ObjectMapper mapper, Clock clock) {
        this.poRepository = poRepository;
        this.poLineRepository = poLineRepository;
        this.grRepository = grRepository;
        this.grLineRepository = grLineRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.ruleRepository = ruleRepository;
        this.exceptionRepository = exceptionRepository;
        this.postingRepository = postingRepository;
        this.batchRepository = batchRepository;
        this.outboxRepository = outboxRepository;
        this.bus = bus;
        this.idempotency = idempotency;
        this.audit = audit;
        this.metrics = metrics;
        this.mapper = mapper;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- ingest

    @Transactional
    public String createPo(Api.CreatePoRequest req, String idemKey) {
        String existing = idempotency.begin(idemKey, "PO");
        if (existing != null) {
            return existing;
        }
        try {
            if (poRepository.findByPoNumber(req.poNumber()).isPresent()) {
                throw new Problems.Conflict("PO number already exists");
            }
            PurchaseOrder po = new PurchaseOrder(UUID.randomUUID().toString(), req.poNumber(),
                    req.supplierId(), req.supplierName(), req.currency(),
                    Instant.now(clock), Instant.now(clock));
            poRepository.save(po);
            int lineNo = 1;
            for (Api.CreatePoRequest.Line line : req.lines()) {
                poLineRepository.save(new PurchaseOrderLine(UUID.randomUUID().toString(),
                        po.getId(), lineNo++, line.itemCode(), line.description(),
                        line.quantity(), line.unitPrice()));
            }
            audit.record("PO_CREATED", "PURCHASE_ORDER", po.getId(),
                    req.poNumber() + " (" + req.lines().size() + " lines)");
            idempotency.complete(idemKey, po.getId(), 201);
            return po.getId();
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    @Transactional
    public String createGr(Api.CreateGrRequest req, String idemKey) {
        String existing = idempotency.begin(idemKey, "GR");
        if (existing != null) {
            return existing;
        }
        try {
            PurchaseOrder po = poRepository.findById(req.poId())
                    .orElseThrow(() -> new Problems.NotFound("PO not found"));
            GoodsReceipt gr = new GoodsReceipt(UUID.randomUUID().toString(), req.grNumber(),
                    po.getId(), po.getSupplierId(), Instant.now(clock));
            grRepository.save(gr);
            for (Api.CreateGrRequest.GrLine line : req.lines()) {
                PurchaseOrderLine pol = poLineRepository.findById(line.poLineId())
                        .orElseThrow(() -> new Problems.NotFound("PO line not found"));
                grLineRepository.save(new GoodsReceiptLine(UUID.randomUUID().toString(),
                        gr.getId(), pol.getId(), line.quantityReceived()));
                pol.creditReceipt(line.quantityReceived());
                poLineRepository.save(pol);
            }
            audit.record("GR_POSTED", "GOODS_RECEIPT", gr.getId(), req.grNumber());
            idempotency.complete(idemKey, gr.getId(), 201);
            return gr.getId();
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    @Transactional
    public Api.InvoiceView ingestInvoice(Api.CreateInvoiceRequest req, String idemKey) {
        String existing = idempotency.begin(idemKey, "INVOICE");
        if (existing != null) {
            return view(load(existing));
        }
        try {
            if (invoiceRepository.findByInvoiceNumberAndSupplierId(
                    req.invoiceNumber(), req.supplierId()).isPresent()) {
                throw new Problems.Conflict("Invoice number already exists for this supplier");
            }
            Invoice invoice = new Invoice(UUID.randomUUID().toString(), req.invoiceNumber(),
                    req.supplierId(), req.supplierName(), req.currency(), req.totalAmount(),
                    req.invoiceDate(), req.dueDate(), Instant.now(clock));
            invoiceRepository.save(invoice);
            for (Api.CreateInvoiceRequest.InvLine line : req.lines()) {
                invoiceLineRepository.save(new InvoiceLine(UUID.randomUUID().toString(),
                        invoice.getId(), line.itemCode(), line.quantity(), line.unitPrice(),
                        line.lineTotal()));
            }
            metrics.incrementInvoicesIngested();
            audit.record("INVOICE_INGESTED", "INVOICE", invoice.getId(), req.invoiceNumber());
            idempotency.complete(idemKey, invoice.getId(), 201);
            match(invoice.getId());
            return view(invoice);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    // ---------------------------------------------------------------- matching

    /** Runs the three-way match for one invoice; creates exceptions; transitions state. */
    @Transactional
    public Api.InvoiceView match(String invoiceId) {
        Invoice invoice = load(invoiceId);
        if (invoice.getStatus() == Invoice.Status.POSTED
                || invoice.getStatus() == Invoice.Status.REJECTED) {
            return view(invoice);
        }
        metrics.matchDuration().record(() -> doMatch(invoice));
        return view(invoice);
    }

    private void doMatch(Invoice invoice) {
        List<PurchaseOrder> pos = poRepository.findBySupplierId(invoice.getSupplierId());
        Map<String, List<PurchaseOrderLine>> poLinesByPo = new HashMap<>();
        Map<String, List<GoodsReceipt>> receiptsByPo = new HashMap<>();
        Map<String, List<GoodsReceiptLine>> receiptLinesByGr = new HashMap<>();
        for (PurchaseOrder po : pos) {
            poLinesByPo.put(po.getId(), poLineRepository.findByPoId(po.getId()));
            receiptsByPo.put(po.getId(), grRepository.findByPoId(po.getId()));
        }
        for (List<GoodsReceipt> grs : receiptsByPo.values()) {
            for (GoodsReceipt gr : grs) {
                receiptLinesByGr.put(gr.getId(), grLineRepository.findByGrId(gr.getId()));
            }
        }
        MatchingEngine.MatchResult result = MatchingEngine.match(invoice,
                invoiceLineRepository.findByInvoiceId(invoice.getId()), pos, poLinesByPo,
                receiptsByPo, receiptLinesByGr, ruleRepository.findByActiveTrue());

        // dedupe exceptions per invoice+type (reprocessing is idempotent)
        int created = 0;
        for (MatchingEngine.Finding f : result.findings()) {
            if (exceptionRepository.findByInvoiceIdAndExceptionType(
                    invoice.getId(), f.type().name()).isEmpty()) {
                exceptionRepository.save(new MatchException(UUID.randomUUID().toString(),
                        invoice.getId(), f.type(),
                        f.severity() == MatchingEngine.Severity.CRITICAL
                                ? Severity.CRITICAL : Severity.WARNING,
                        f.detail(), Instant.now(clock)));
                metrics.incrementExceptionsCreated();
                created++;
            }
        }
        metrics.setOpenExceptions(exceptionRepository.countByStatus("OPEN"));

        if (result.criticals().isEmpty()) {
            // warnings only or clean match
            for (MatchException ex : exceptionRepository.findByInvoiceIdOrderByCreatedAtAsc(invoice.getId())) {
                if (ex.getStatus() == Status.OPEN && ex.getSeverity() == Severity.WARNING) {
                    ex.resolve(Status.RESOLVED, "matching-engine", Instant.now(clock),
                            "Auto-resolved within tolerance");
                    exceptionRepository.save(ex);
                }
            }
            invoice.markMatched();
            invoiceRepository.save(invoice);
            metrics.incrementInvoicesMatched();
            bus.publish(new InvoiceMatched(UUID.randomUUID().toString(), Instant.now(clock),
                    invoice.getId(), invoice.getInvoiceNumber()));
        } else {
            invoice.markException();
            invoiceRepository.save(invoice);
        }
        outbox(invoice.getId(), "INVOICE_MATCH_EVALUATED", Map.of(
                "invoiceId", invoice.getId(),
                "status", invoice.getStatus().name(),
                "exceptionsCreated", created));
        if (created > 0) {
            log.info("Invoice {} match: status={} exceptionsCreated={}",
                    invoice.getInvoiceNumber(), invoice.getStatus(), created);
        }
    }

    /** Re-processes every non-terminal invoice (nightly reconciliation batch, idempotent). */
    @Transactional
    public Api.BatchView runBatch() {
        BatchRun batch = new BatchRun(UUID.randomUUID().toString(), Instant.now(clock));
        batchRepository.save(batch);
        int processed = 0;
        int exceptions = 0;
        for (Invoice invoice : invoiceRepository.findByStatus("NEW")) {
            doMatch(invoice);
            processed++;
        }
        for (Invoice invoice : invoiceRepository.findByStatus("EXCEPTION")) {
            doMatch(invoice);
            processed++;
        }
        exceptions = (int) exceptionRepository.countByStatus("OPEN");
        int postings = postApproved(batch.getId());
        batch.complete(processed, exceptions, postings, Instant.now(clock));
        batchRepository.save(batch);
        audit.record("BATCH_COMPLETED", "BATCH_RUN", batch.getId(),
                "processed=" + processed + " exceptions=" + exceptions + " postings=" + postings);
        return new Api.BatchView(batch.getId(), batch.getStartedAt(), batch.getCompletedAt(),
                batch.getInvoicesProcessed(), batch.getExceptionsCreated(),
                batch.getPostingsCreated(), batch.getStatus());
    }

    // ---------------------------------------------------------------- exceptions

    @Transactional
    public Api.ExceptionView assign(String exceptionId, String assignee) {
        MatchException ex = loadException(exceptionId);
        if (ex.getStatus() != Status.OPEN) {
            throw new Problems.Conflict("Exception is not open");
        }
        ex.assign(assignee);
        exceptionRepository.save(ex);
        audit.record("EXCEPTION_ASSIGNED", "EXCEPTION", exceptionId, assignee);
        return view(ex);
    }

    /** Waive a CRITICAL exception — four-eyes: requires AP_MANAGER, waiver is audited. */
    @Transactional
    public Api.ExceptionView waive(String exceptionId, String note, String idemKey) {
        String existing = idempotency.begin(idemKey, "EXCEPTION_WAIVE");
        MatchException ex;
        try {
            ex = loadException(exceptionId);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
        if (existing != null) {
            return view(ex);
        }
        if (ex.getStatus() != Status.OPEN) {
            throw new Problems.Conflict("Exception is not open");
        }
        if (!SecurityUtil.hasRole(Roles.AP_MANAGER) && !SecurityUtil.hasRole(Roles.ADMIN)) {
            throw new Problems.Conflict("Waivers require AP_MANAGER approval (four-eyes)");
        }
        ex.resolve(Status.WAIVED, SecurityUtil.currentUsername(), Instant.now(clock), note);
        exceptionRepository.save(ex);
        metrics.incrementExceptionsWaived();
        metrics.setOpenExceptions(exceptionRepository.countByStatus("OPEN"));
        audit.record("EXCEPTION_WAIVED", "EXCEPTION", exceptionId, note);
        idempotency.complete(idemKey, exceptionId, 200);
        maybeApprove(ex.getInvoiceId());
        return view(ex);
    }

    /** Reject an exception: the invoice is rejected and excluded from posting. */
    @Transactional
    public Api.ExceptionView reject(String exceptionId, String note) {
        MatchException ex = loadException(exceptionId);
        if (ex.getStatus() != Status.OPEN) {
            throw new Problems.Conflict("Exception is not open");
        }
        ex.resolve(Status.REJECTED, SecurityUtil.currentUsername(), Instant.now(clock), note);
        exceptionRepository.save(ex);
        Invoice invoice = load(ex.getInvoiceId());
        invoice.markRejected();
        invoiceRepository.save(invoice);
        metrics.setOpenExceptions(exceptionRepository.countByStatus("OPEN"));
        audit.record("EXCEPTION_REJECTED", "EXCEPTION", exceptionId, note);
        return view(ex);
    }

    /** After all criticals are decided, the invoice moves to APPROVED (posting-eligible). */
    private void maybeApprove(String invoiceId) {
        List<MatchException> exceptions =
                exceptionRepository.findByInvoiceIdOrderByCreatedAtAsc(invoiceId);
        boolean allDecided = exceptions.stream()
                .noneMatch(ex -> ex.getStatus() == Status.OPEN);
        boolean anyRejected = exceptions.stream()
                .anyMatch(ex -> ex.getStatus() == Status.REJECTED);
        Invoice invoice = load(invoiceId);
        if (allDecided && !anyRejected && invoice.getStatus() == Invoice.Status.EXCEPTION) {
            invoice.markApproved();
            invoiceRepository.save(invoice);
            audit.record("INVOICE_APPROVED", "INVOICE", invoiceId,
                    "All exceptions resolved — ready for posting");
        }
    }

    // ---------------------------------------------------------------- posting

    /** Posts APPROVED invoices: debit GRNI / credit AP, outbox events, idempotent. */
    @Transactional
    public int postApproved(String batchId) {
        int created = 0;
        for (Invoice invoice : invoiceRepository.findByStatus("APPROVED")) {
            if (!postingRepository.findByInvoiceId(invoice.getId()).isEmpty()) {
                continue;
            }
            BigDecimal amount = invoice.getTotalAmount();
            postingRepository.save(new GlPosting(UUID.randomUUID().toString(), invoice.getId(),
                    batchId, ACCOUNT_GRNI, amount, BigDecimal.ZERO, Instant.now(clock)));
            postingRepository.save(new GlPosting(UUID.randomUUID().toString(), invoice.getId(),
                    batchId, ACCOUNT_AP, BigDecimal.ZERO, amount, Instant.now(clock)));
            for (GlPosting posting : postingRepository.findByInvoiceId(invoice.getId())) {
                posting.post(Instant.now(clock));
                postingRepository.save(posting);
            }
            invoice.markPosted();
            invoiceRepository.save(invoice);
            metrics.incrementPostingsCreated();
            outbox(invoice.getId(), "INVOICE_POSTED", Map.of(
                    "invoiceId", invoice.getId(),
                    "invoiceNumber", invoice.getInvoiceNumber(),
                    "amount", amount.toString()));
            bus.publish(new InvoicePosted(UUID.randomUUID().toString(), Instant.now(clock),
                    invoice.getId(), invoice.getInvoiceNumber()));
            created += 2;
        }
        return created;
    }

    @Scheduled(fixedDelayString = "60000", initialDelayString = "30000")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
    public void scheduledPosting() {
        BatchRun batch = new BatchRun(UUID.randomUUID().toString(), Instant.now(clock));
        batchRepository.save(batch);
        int postings = postApproved(batch.getId());
        batch.complete(0, 0, postings, Instant.now(clock));
        batchRepository.save(batch);
        if (postings > 0) {
            log.info("Scheduled posting batch: {} GL postings", postings);
        }
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public Api.PoView poView(String poId) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new Problems.NotFound("PO not found"));
        List<Api.PoLineView> lines = poLineRepository.findByPoId(poId).stream()
                .map(l -> new Api.PoLineView(l.getId(), l.getLineNo(), l.getItemCode(),
                        l.getDescription(), l.getQuantity(), l.getUnitPrice(),
                        l.getReceivedQty(), l.getInvoicedQty()))
                .toList();
        return new Api.PoView(po.getId(), po.getPoNumber(), po.getSupplierId(),
                po.getSupplierName(), po.getCurrency(), po.getStatus().name(),
                po.getIssuedAt(), lines);
    }

    @Transactional(readOnly = true)
    public PageResponse<Api.InvoiceView> invoices(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        var result = (status == null || status.isBlank())
                ? invoiceRepository.findAll(pr)
                : invoiceRepository.findByStatus(status.toUpperCase(), pr);
        return PageResponse.from(result.map(this::view));
    }

    @Transactional(readOnly = true)
    public PageResponse<Api.ExceptionView> exceptions(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt"));
        var result = (status == null || status.isBlank())
                ? exceptionRepository.findAll(pr)
                : exceptionRepository.findByStatus(status.toUpperCase(), pr);
        return PageResponse.from(result.map(this::view));
    }

    @Transactional(readOnly = true)
    public List<Api.RuleView> toleranceRules() {
        return ruleRepository.findByActiveTrue().stream()
                .map(r -> new Api.RuleView(r.getId(), r.getRuleType().name(),
                        r.getTolerancePct(), r.getAction(), r.isActive()))
                .toList();
    }

    @Transactional
    public Api.RuleView updateRule(String ruleId, Api.UpdateRuleRequest req) {
        ToleranceRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new Problems.NotFound("Tolerance rule not found"));
        if (!"WARN".equals(req.action()) && !"BLOCK".equals(req.action())
                && !"AUTO_POST".equals(req.action())) {
            throw new Problems.BadRequest("Action must be WARN, BLOCK or AUTO_POST");
        }
        rule.update(req.tolerancePct(), req.action());
        ruleRepository.save(rule);
        audit.record("RULE_UPDATED", "TOLERANCE_RULE", ruleId,
                rule.getRuleType() + " -> " + req.tolerancePct() + "%/" + req.action());
        return new Api.RuleView(rule.getId(), rule.getRuleType().name(),
                rule.getTolerancePct(), rule.getAction(), rule.isActive());
    }

    // ---------------------------------------------------------------- helpers

    private Invoice load(String id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Invoice not found"));
    }

    private MatchException loadException(String id) {
        return exceptionRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Exception not found"));
    }

    private Api.InvoiceView view(Invoice invoice) {
        return new Api.InvoiceView(invoice.getId(), invoice.getInvoiceNumber(),
                invoice.getSupplierName(), invoice.getCurrency(), invoice.getTotalAmount(),
                invoice.getStatus().name(), invoice.getInvoiceDate(), invoice.getCreatedAt());
    }

    private Api.ExceptionView view(MatchException ex) {
        return new Api.ExceptionView(ex.getId(), ex.getInvoiceId(), ex.getType().name(),
                ex.getSeverity().name(), ex.getDetailJson(), ex.getStatus().name(),
                ex.getAssignedTo(), ex.getCreatedAt(), ex.getResolvedAt(), ex.getResolvedBy(),
                ex.getResolutionNote());
    }

    private void outbox(String aggregateId, String eventType, Map<String, Object> payload) {
        try {
            outboxRepository.save(new OutboxRecord(UUID.randomUUID().toString(), eventType,
                    mapper.writeValueAsString(payload), Instant.now(clock)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox serialization failed", e);
        }
    }

    public record InvoiceMatched(String eventId, Instant occurredAt, String invoiceId,
                                 String invoiceNumber) implements DomainEvent {
    }

    public record InvoicePosted(String eventId, Instant occurredAt, String invoiceId,
                                String invoiceNumber) implements DomainEvent {
    }
}
