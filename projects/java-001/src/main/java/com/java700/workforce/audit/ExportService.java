package com.java700.workforce.audit;





import com.java700.workforce.access.GrantRepository;
import com.java700.workforce.common.api.Problems;
import com.java700.workforce.common.audit.AuditLogService;
import com.java700.workforce.common.web.IdempotencyService;
import com.java700.workforce.compliance.ViolationRepository;
import com.java700.workforce.evidence.EvidenceEntry;
import com.java700.workforce.evidence.EvidenceRepository;
import com.java700.workforce.evidence.EvidenceService;
import com.java700.workforce.events.AccessEventRepository;
import com.java700.workforce.messaging.DomainEvent;
import com.java700.workforce.messaging.DomainEventBus;
import com.java700.workforce.messaging.DomainEventHandler;
import com.java700.workforce.observability.Metrics;
import com.java700.workforce.security.SecurityUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Auditor-grade exports: JSONL evidence bundle scoped by user/time, an HMAC-SHA256
 * signature over the bundle bytes, and a manifest. Builds run asynchronously over the
 * bus; a rescan re-queues stale PENDING jobs (pragmatic outbox).
 */
@Service
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ExportJobRepository jobRepository;
    private final EvidenceRepository evidenceRepository;
    private final ViolationRepository violationRepository;
    private final GrantRepository grantRepository;
    private final AccessEventRepository eventRepository;
    private final EvidenceService evidenceService;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;
    private final String signingKey;
    private final Path outboxDir;

    public ExportService(ExportJobRepository jobRepository, EvidenceRepository evidenceRepository,
                         ViolationRepository violationRepository, GrantRepository grantRepository,
                         AccessEventRepository eventRepository, EvidenceService evidenceService,
                         DomainEventBus bus, IdempotencyService idempotency, AuditLogService audit,
                         Metrics metrics, Clock clock,
                         @Value("${app.security.export.signing-key}") String signingKey,
                         @Value("${app.security.export.outbox-dir:./data/exports}") String outboxDir) {
        this.jobRepository = jobRepository;
        this.evidenceRepository = evidenceRepository;
        this.violationRepository = violationRepository;
        this.grantRepository = grantRepository;
        this.eventRepository = eventRepository;
        this.evidenceService = evidenceService;
        this.bus = bus;
        this.idempotency = idempotency;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
        this.signingKey = signingKey;
        this.outboxDir = Path.of(outboxDir);
    }

    @Transactional
    public ExportApi.ExportJobView createExport(String scopeUserId, Instant from, Instant to,
                                                String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "AUDIT_EXPORT");
        if (existing != null) {
            ExportJob job = jobRepository.findById(existing)
                    .orElseThrow(() -> new Problems.NotFound("Export job not found"));
            return ExportApi.ExportJobView.from(job);
        }
        if (to.isBefore(from)) {
            idempotency.abandon(idempotencyKey);
            throw new Problems.BadRequest("rangeTo must be after rangeFrom");
        }
        Long startSeq = evidenceRepository.findTopByOrderBySeqDesc()
                .map(EvidenceEntry::getSeq).orElse(0L);
        ExportJob job = new ExportJob(UUID.randomUUID().toString(), SecurityUtil.currentUsername(),
                scopeUserId, from, to, startSeq, Instant.now(clock));
        jobRepository.save(job);
        metrics.incrementExportJobs();
        audit.record("EXPORT_REQUESTED", "EXPORT_JOB", job.getId(),
                "Scope user=" + (scopeUserId == null ? "ALL" : scopeUserId));
        bus.publish(new ExportRequested(UUID.randomUUID().toString(), Instant.now(clock), job.getId()));
        idempotency.complete(idempotencyKey, job.getId(), 202);
        return ExportApi.ExportJobView.from(job);
    }

    /** Builds the signed bundle; idempotent on job state. */
    @Transactional
    public void build(String jobId) {
        ExportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new Problems.NotFound("Export job not found"));
        if (job.getStatus() != ExportJob.Status.PENDING) {
            return;
        }
        metrics.exportDuration().record(() -> {
            try {
                Files.createDirectories(outboxDir);
                StringBuilder sb = new StringBuilder();
                List<EvidenceEntry> entries = evidenceRepository.findAll(
                        org.springframework.data.domain.Sort.by("seq")).stream()
                        .filter(e -> e.getSeq() >= job.getStartSeq())
                        .filter(e -> e.getOccurredAt().isBefore(job.getRangeTo()))
                        .toList();
                boolean scoped = job.getScopeUserId() != null && !"*".equals(job.getScopeUserId());
                for (EvidenceEntry e : entries) {
                    if (scoped && !e.getAggregateId().equals(job.getScopeUserId())) {
                        continue;
                    }
                    sb.append(e.getPayload()).append('\n');
                }
                if (scoped) {
                    violationRepository.findByUserId(job.getScopeUserId(),
                            org.springframework.data.domain.PageRequest.of(0, 1000,
                                    org.springframework.data.domain.Sort.by("detectedAt")))
                            .forEach(v -> sb.append("{\"type\":\"VIOLATION\",\"id\":\"")
                                    .append(v.getId()).append("\",\"ruleType\":\"")
                                    .append(v.getRuleType()).append("\",\"severity\":\"")
                                    .append(v.getSeverity()).append("\",\"status\":\"")
                                    .append(v.getStatus().name()).append("\",\"detectedAt\":\"")
                                    .append(v.getDetectedAt()).append("\"}\n"));
                    grantRepository.findByUserId(job.getScopeUserId(),
                            org.springframework.data.domain.PageRequest.of(0, 1000))
                            .forEach(g -> sb.append("{\"type\":\"GRANT\",\"id\":\"")
                                    .append(g.getId()).append("\",\"resourceName\":\"")
                                    .append(g.getResourceName()).append("\",\"roles\":")
                                    .append(g.getRolesJson()).append(",\"status\":\"")
                                    .append(g.getStatus().name()).append("\"}\n"));
                    eventRepository.findAll().stream()
                            .filter(ev -> job.getScopeUserId().equals(ev.getUserId()))
                            .limit(5000)
                            .forEach(ev -> sb.append("{\"type\":\"ACCESS_EVENT\",\"id\":\"")
                                    .append(ev.getId()).append("\",\"userId\":\"")
                                    .append(ev.getUserId()).append("\"}\n"));
                }
                byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
                String hmac = hexHmac(data);
                Path file = outboxDir.resolve(jobId + ".jsonl");
                Files.write(file, data);
                Long endSeq = evidenceRepository.findTopByOrderBySeqDesc()
                        .map(EvidenceEntry::getSeq).orElse(0L);
                job.complete(endSeq, file.toString(), hmac, Instant.now(clock));
                jobRepository.save(job);
                evidenceService.append("EXPORT_JOB", jobId, "EXPORT_COMPLETED", job.getRequestedBy(),
                        Map.of("bytes", data.length, "startSeq", job.getStartSeq(), "endSeq", endSeq,
                                "hmac", hmac));
                audit.record("EXPORT_COMPLETED", "EXPORT_JOB", jobId,
                        "Bundle " + data.length + " bytes, seq " + job.getStartSeq() + ".." + endSeq);
                log.info("Export {} completed: {} bytes, hmac {}", jobId, data.length, hmac);
            } catch (IOException e) {
                job.fail("I/O error: " + e.getMessage(), Instant.now(clock));
                jobRepository.save(job);
                throw new Problems.ServiceUnavailable("Export build failed: " + e.getMessage());
            }
        });
    }

    @Transactional(readOnly = true)
    public byte[] download(String jobId) {
        ExportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new Problems.NotFound("Export job not found"));
        if (job.getStatus() != ExportJob.Status.COMPLETED) {
            throw new Problems.Conflict("Export is not completed yet");
        }
        try {
            return Files.readAllBytes(Path.of(job.getFilePath()));
        } catch (IOException e) {
            throw new Problems.ServiceUnavailable("Export bundle unreadable: " + e.getMessage());
        }
    }

    /** Re-verifies the bundle's HMAC server-side. */
    @Transactional(readOnly = true)
    public ExportApi.VerifyResponse verify(String jobId) {
        ExportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new Problems.NotFound("Export job not found"));
        if (job.getStatus() != ExportJob.Status.COMPLETED) {
            throw new Problems.Conflict("Export is not completed yet");
        }
        try {
            byte[] data = Files.readAllBytes(Path.of(job.getFilePath()));
            boolean valid = hexHmac(data).equals(job.getHmac());
            return new ExportApi.VerifyResponse(valid, HMAC_ALGORITHM,
                    job.getStartSeq(), job.getEndSeq());
        } catch (IOException e) {
            throw new Problems.ServiceUnavailable("Export bundle unreadable");
        }
    }

    @Transactional(readOnly = true)
    public ExportApi.ExportJobView get(String jobId) {
        return ExportApi.ExportJobView.from(jobRepository.findById(jobId)
                .orElseThrow(() -> new Problems.NotFound("Export job not found")));
    }

    /** Outbox rescan: re-queue PENDING jobs that never got a worker (crash recovery). */
    @Scheduled(fixedDelayString = "120000", initialDelayString = "60000")
    public void rescanStaleJobs() {
        List<ExportJob> stale = jobRepository.findByStatusAndCreatedAtBefore(
                "PENDING", Instant.now(clock).minusSeconds(120));
        for (ExportJob job : stale) {
            bus.publish(new ExportRequested(UUID.randomUUID().toString(), Instant.now(clock), job.getId()));
        }
    }

    private String hexHmac(byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(data));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }

    /** Worker: consumes ExportRequested (broker or direct bus) and builds the bundle. */
    @Component
    public static class ExportWorker implements DomainEventHandler<ExportRequested> {

        private final ExportService service;

        public ExportWorker(ExportService service) {
            this.service = service;
        }

        @Override
        public Class<ExportRequested> supportedType() {
            return ExportRequested.class;
        }

        @Override
        public void handle(ExportRequested event) {
            service.build(event.jobId());
        }
    }

    public record ExportRequested(String eventId, Instant occurredAt, String jobId)
            implements DomainEvent {
    }
}
