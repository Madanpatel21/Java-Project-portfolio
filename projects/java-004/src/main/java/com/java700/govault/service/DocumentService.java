package com.java700.govault.service;

import com.java700.govault.common.api.PageResponse;
import com.java700.govault.common.api.Problems;
import com.java700.govault.common.audit.AuditLogService;
import com.java700.govault.common.web.IdempotencyService;
import com.java700.govault.domain.DispositionProof;
import com.java700.govault.domain.DispositionProofRepository;
import com.java700.govault.domain.Document;
import com.java700.govault.domain.DocumentRepository;
import com.java700.govault.domain.HoldEntry;
import com.java700.govault.domain.HoldEntryRepository;
import com.java700.govault.domain.LegalHold;
import com.java700.govault.domain.LegalHoldRepository;
import com.java700.govault.domain.RetentionRule;
import com.java700.govault.domain.RetentionRuleRepository;
import com.java700.govault.messaging.DomainEvent;
import com.java700.govault.messaging.DomainEventBus;
import com.java700.govault.observability.Metrics;
import com.java700.govault.security.Roles;
import com.java700.govault.security.SecurityUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Document governance: upload → quarantine → classify → retention-based disposition with
 * legal-hold protection and append-only disposition proofs.
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final RetentionRuleRepository ruleRepository;
    private final LegalHoldRepository holdRepository;
    private final HoldEntryRepository holdEntryRepository;
    private final DispositionProofRepository proofRepository;
    private final TextExtractor textExtractor;
    private final ContentHasher hasher;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;
    private final Path contentDir;

    public DocumentService(DocumentRepository documentRepository,
                           RetentionRuleRepository ruleRepository,
                           LegalHoldRepository holdRepository,
                           HoldEntryRepository holdEntryRepository,
                           DispositionProofRepository proofRepository,
                           TextExtractor textExtractor, ContentHasher hasher,
                           DomainEventBus bus, IdempotencyService idempotency,
                           AuditLogService audit, Metrics metrics, Clock clock,
                           @Value("${app.govault.content-dir:./data/content}") String contentDir) {
        this.documentRepository = documentRepository;
        this.ruleRepository = ruleRepository;
        this.holdRepository = holdRepository;
        this.holdEntryRepository = holdEntryRepository;
        this.proofRepository = proofRepository;
        this.textExtractor = textExtractor;
        this.hasher = hasher;
        this.bus = bus;
        this.idempotency = idempotency;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
        this.contentDir = Path.of(contentDir);
    }

    @Transactional
    public Api.DocumentView upload(MultipartFile file, String title, String idemKey) {
        String existing = idempotency.begin(idemKey, "DOCUMENT_UPLOAD");
        if (existing != null) {
            return view(load(existing));
        }
        try {
            byte[] content = read(file);
            String hash = hasher.sha256(content);
            String originalName = file.getOriginalFilename() == null ? "unnamed" : file.getOriginalFilename();
            Document doc = new Document(UUID.randomUUID().toString(),
                    title == null || title.isBlank() ? originalName : title,
                    originalName, file.getContentType(), content.length,
                    SecurityUtil.currentUserId(), SecurityUtil.currentUsername(), hash,
                    textExtractor.extract(originalName, content),
                    Instant.now(clock));
            documentRepository.save(doc);
            persistContent(doc.getId(), content);
            metrics.incrementUploaded();
            metrics.setQuarantined(documentRepository.findByStatus("QUARANTINED").size());
            audit.record("DOCUMENT_UPLOADED", "DOCUMENT", doc.getId(),
                    doc.getFileName() + " sha256=" + hash + " -> QUARANTINED");
            bus.publish(new DocumentUploaded(UUID.randomUUID().toString(), Instant.now(clock),
                    doc.getId(), doc.getFileName(), hash));
            idempotency.complete(idemKey, doc.getId(), 201);
            return view(doc);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    @Transactional
    public Api.DocumentView classify(String documentId, String classification,
                                     String retentionClass, String idemKey) {
        String existing = idempotency.begin(idemKey, "DOCUMENT_CLASSIFY");
        // validate first: reject malformed input before any data access
        Document.Classification c;
        try {
            c = parseClassification(classification);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
        if (ruleRepository.findByRetentionClass(retentionClass).isEmpty()) {
            idempotency.abandon(idemKey);
            throw new Problems.BadRequest("Unknown retention class: " + retentionClass);
        }
        Document doc;
        try {
            doc = load(documentId);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
        if (existing != null) {
            return view(doc);
        }
        try {
            doc.classify(c, retentionClass);
            documentRepository.save(doc);
            metrics.incrementClassified();
            metrics.setQuarantined(documentRepository.findByStatus("QUARANTINED").size());
            audit.record("DOCUMENT_CLASSIFIED", "DOCUMENT", documentId,
                    classification + "/" + retentionClass + " by " + SecurityUtil.currentUsername());
            idempotency.complete(idemKey, documentId, 200);
            return view(doc);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    // ---------------------------------------------------------------- legal holds

    @Transactional
    public Api.HoldView createHold(String name, String reason, String idemKey) {
        String existing = idempotency.begin(idemKey, "LEGAL_HOLD");
        if (existing != null) {
            return holdView(loadHold(existing));
        }
        try {
            LegalHold hold = new LegalHold(UUID.randomUUID().toString(), name, reason,
                    SecurityUtil.currentUsername(), Instant.now(clock));
            holdRepository.save(hold);
            audit.record("HOLD_CREATED", "LEGAL_HOLD", hold.getId(), name + ": " + reason);
            idempotency.complete(idemKey, hold.getId(), 201);
            return holdView(hold);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    @Transactional
    public Api.HoldView applyToDocument(String holdId, String documentId) {
        LegalHold hold = loadHold(holdId);
        if (hold.getStatus() != LegalHold.Status.ACTIVE) {
            throw new Problems.Conflict("Hold is released");
        }
        Document doc = load(documentId);
        if (doc.getStatus() == Document.Status.DISPOSED) {
            throw new Problems.Conflict("Disposed documents cannot be placed on hold");
        }
        holdEntryRepository.findByHoldIdAndDocumentId(holdId, documentId).ifPresentOrElse(
                entry -> {
                    throw new Problems.Conflict("Document is already on this hold");
                },
                () -> {
                    holdEntryRepository.save(new HoldEntry(UUID.randomUUID().toString(),
                            holdId, documentId));
                    doc.placeHold();
                    documentRepository.save(doc);
                    metrics.incrementHoldsApplied();
                    audit.record("HOLD_APPLIED", "DOCUMENT", documentId,
                            "Hold " + hold.getName() + " applied by " + SecurityUtil.currentUsername());
                });
        return holdView(hold);
    }

    @Transactional
    public Api.HoldView release(String holdId) {
        LegalHold hold = loadHold(holdId);
        if (hold.getStatus() != LegalHold.Status.ACTIVE) {
            throw new Problems.Conflict("Hold is already released");
        }
        hold.release(Instant.now(clock));
        holdRepository.save(hold);
        for (HoldEntry entry : holdEntryRepository.findByHoldId(holdId)) {
            documentRepository.findById(entry.getDocumentId()).ifPresent(doc -> {
                boolean otherActiveHolds = holdEntryRepository.findByDocumentId(doc.getId())
                        .stream()
                        .anyMatch(e -> holdRepository.findById(e.getHoldId())
                                .map(h -> h.getStatus() == LegalHold.Status.ACTIVE)
                                .orElse(false));
                if (!otherActiveHolds) {
                    doc.releaseHold();
                    documentRepository.save(doc);
                }
            });
        }
        audit.record("HOLD_RELEASED", "LEGAL_HOLD", holdId,
                "Released by " + SecurityUtil.currentUsername());
        return holdView(hold);
    }

    // ---------------------------------------------------------------- disposition

    /** Retention scan: dispose past-retention ACTIVE documents that are not on hold. */
    @Transactional
    public Api.ScanResult scan() {
        Instant now = Instant.now(clock);
        int disposed = 0;
        int protectedDocs = 0;
        for (Document doc : documentRepository.findByStatus("ACTIVE")) {
            RetentionRule rule = ruleRepository.findByRetentionClass(doc.getRetentionClass())
                    .orElse(null);
            if (rule == null || rule.getRetentionDays() < 0) {
                continue; // permanent retention
            }
            Instant due = doc.getUploadedAt().plus(rule.getRetentionDays(), ChronoUnit.DAYS);
            if (now.isBefore(due)) {
                continue;
            }
            if (doc.isLegalHold()) {
                protectedDocs++;
                continue;
            }
            if ("REVIEW".equals(rule.getAction())) {
                continue; // human review required before disposition
            }
            dispose(doc, rule.getAction().equals("ARCHIVE") ? "ARCHIVED" : "DISPOSED");
            disposed++;
        }
        metrics.setQuarantined(documentRepository.findByStatus("QUARANTINED").size());
        if (disposed > 0 || protectedDocs > 0) {
            log.info("Retention scan: {} disposed, {} protected by holds", disposed, protectedDocs);
        }
        return new Api.ScanResult(disposed, protectedDocs);
    }

    private void dispose(Document doc, String disposition) {
        doc.dispose(Instant.now(clock));
        documentRepository.save(doc);
        proofRepository.save(new DispositionProof(UUID.randomUUID().toString(), doc.getId(),
                doc.getTitle(), doc.getContentHash(), doc.getRetentionClass(),
                Instant.now(clock), SecurityUtil.currentUsername(), disposition));
        deleteContent(doc.getId());
        metrics.incrementDisposed();
        audit.record("DOCUMENT_DISPOSED", "DOCUMENT", doc.getId(),
                disposition + " with proof; hash " + doc.getContentHash());
        bus.publish(new DocumentDisposed(UUID.randomUUID().toString(), Instant.now(clock),
                doc.getId(), doc.getContentHash()));
    }

    /**
     * Dev/test simulation facility: shifts a document's upload timestamp back so the
     * retention scan exercises the real disposition path. Available ONLY in the dev
     * profile (see DevTimeController); never deployed in production profiles.
     */
    @Transactional
    public void backdateForTest(String documentId, int days) {
        Document doc = load(documentId);
        if (doc.getStatus() == Document.Status.DISPOSED) {
            throw new Problems.Conflict("Disposed documents cannot be backdated");
        }
        Instant shifted = doc.getUploadedAt().minus(days, ChronoUnit.DAYS);
        documentRepository.backdateUploadedAt(documentId, shifted);
        audit.record("DOCUMENT_BACKDATED_TEST", "DOCUMENT", documentId,
                "Test backdate " + days + " days by " + SecurityUtil.currentUsername());
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public PageResponse<Api.DocumentView> documents(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100),
                Sort.by("uploadedAt").descending());
        var result = (status == null || status.isBlank())
                ? documentRepository.findAll(pr)
                : documentRepository.findByStatus(status.toUpperCase(), pr);
        return PageResponse.from(result.map(this::view));
    }

    @Transactional(readOnly = true)
    public PageResponse<Api.DocumentView> search(String query, int page, int size) {
        String q = query == null ? "" : query;
        return PageResponse.from(documentRepository
                .findByTitleContainingIgnoreCaseOrExtractedTextContainingIgnoreCase(
                        q, q, PageRequest.of(page, Math.min(size, 100),
                                Sort.by("uploadedAt").descending()))
                .map(this::view));
    }

    @Transactional(readOnly = true)
    public byte[] download(String documentId) {
        Document doc = load(documentId);
        if (doc.getStatus() == Document.Status.DISPOSED) {
            throw new Problems.NotFound("Document has been disposed");
        }
        int clearance = Roles.clearance(currentRole());
        if (doc.getClassification().ordinal() > clearance) {
            throw new Problems.Conflict("Insufficient clearance for this classification");
        }
        try {
            return Files.readAllBytes(contentDir.resolve(documentId));
        } catch (Exception e) {
            throw new Problems.ServiceUnavailable("Content unavailable: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Api.ProofView> dispositionProofs(String documentId) {
        return proofRepository.findByDocumentId(documentId).stream()
                .map(p -> new Api.ProofView(p.getDocumentId(), p.getContentHash(),
                        p.getRetentionClass(), p.getDisposedAt(), p.getExecutor(),
                        p.getDisposition()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Api.HoldView> holds() {
        return holdRepository.findAll().stream().map(this::holdView).toList();
    }

    // ---------------------------------------------------------------- helpers

    private Document load(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Document not found"));
    }

    private LegalHold loadHold(String id) {
        return holdRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Legal hold not found"));
    }

    private static byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            throw new Problems.BadRequest("Cannot read upload: " + e.getMessage());
        }
    }

    private void persistContent(String documentId, byte[] content) {
        try {
            Files.createDirectories(contentDir);
            Files.write(contentDir.resolve(documentId), content);
        } catch (Exception e) {
            throw new Problems.ServiceUnavailable("Cannot persist content: " + e.getMessage());
        }
    }

    private void deleteContent(String documentId) {
        try {
            Files.deleteIfExists(contentDir.resolve(documentId));
        } catch (Exception e) {
            log.warn("Content delete failed for {}: {}", documentId, e.getMessage());
        }
    }

    private String currentRole() {
        return SecurityUtil.hasRole(Roles.RECORDS_MANAGER) ? Roles.RECORDS_MANAGER
                : SecurityUtil.hasRole(Roles.LEGAL_COUNSEL) ? Roles.LEGAL_COUNSEL
                : SecurityUtil.hasRole(Roles.ADMIN) ? Roles.ADMIN
                : SecurityUtil.hasRole(Roles.BUSINESS_OWNER) ? Roles.BUSINESS_OWNER
                : SecurityUtil.hasRole(Roles.AUDITOR) ? Roles.AUDITOR : "UNKNOWN";
    }

    private static Document.Classification parseClassification(String c) {
        try {
            return Document.Classification.valueOf(c.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Problems.BadRequest("Classification must be PUBLIC/INTERNAL/CONFIDENTIAL/RESTRICTED");
        }
    }

    private Api.DocumentView view(Document d) {
        return new Api.DocumentView(d.getId(), d.getTitle(), d.getFileName(),
                d.getContentType(), d.getSizeBytes(), d.getClassification().name(),
                d.getRetentionClass(), d.getOwnerName(), d.getContentHash(),
                d.getStatus().name(), d.isLegalHold(), d.getUploadedAt(), d.getDisposedAt());
    }

    private Api.HoldView holdView(LegalHold h) {
        return new Api.HoldView(h.getId(), h.getName(), h.getReason(), h.getAppliedBy(),
                h.getAppliedAt(), h.getReleasedAt(), h.getStatus().name());
    }

    public record DocumentUploaded(String eventId, Instant occurredAt, String documentId,
                                   String fileName, String contentHash) implements DomainEvent {
    }

    public record DocumentDisposed(String eventId, Instant occurredAt, String documentId,
                                   String contentHash) implements DomainEvent {
    }
}
