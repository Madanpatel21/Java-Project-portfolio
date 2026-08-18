package com.java700.workforce.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.java700.workforce.common.api.Problems;
import com.java700.workforce.WorkforceComplianceApplication;
import com.java700.workforce.evidence.EvidenceService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Auditor exports: HMAC-signed bundles, idempotent creation, tamper detection. */
@SpringBootTest(classes = WorkforceComplianceApplication.class, properties = {"spring.profiles.active=test", "app.security.export.outbox-dir=target/test-exports"})
@ActiveProfiles("test")
class ExportServiceTest {

    @Autowired
    ExportService service;
    @Autowired
    EvidenceService evidence;
    @Autowired
    Clock clock;

    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        com.java700.workforce.common.TestDb.clean(jdbc);
        for (int i = 0; i < 5; i++) {
            evidence.append("TEST", "agg-export", "EVENT_" + i, "tester", Map.of("i", i));
        }
    }

    @Test
    void exportBundleIsSignedAndVerifiable() throws Exception {
        Instant from = Instant.now(clock).minusSeconds(3600);
        Instant to = Instant.now(clock).plusSeconds(3600);
        var job = service.createExport(null, from, to, "export-key-" + UUID.randomUUID());
        // direct bus processes synchronously in dev/test
        assertThat(service.get(job.id()).status()).isEqualTo("COMPLETED");
        assertThat(service.verify(job.id()).valid()).isTrue();
        assertThat(service.download(job.id())).isNotEmpty();
    }

    @Test
    void tamperedBundleFailsVerification() throws Exception {
        Instant from = Instant.now(clock).minusSeconds(3600);
        Instant to = Instant.now(clock).plusSeconds(3600);
        var job = service.createExport(null, from, to, "export-key-" + UUID.randomUUID());
        var completed = service.get(job.id());
        assertThat(completed.status()).isEqualTo("COMPLETED");
        Files.writeString(Path.of(completedFilePath(job.id())), "tampered-bytes");
        assertThat(service.verify(job.id()).valid()).isFalse();
    }

    @Test
    void exportCreationIsIdempotent() {
        Instant from = Instant.now(clock).minusSeconds(3600);
        Instant to = Instant.now(clock).plusSeconds(3600);
        String key = "idem-" + UUID.randomUUID();
        var first = service.createExport(null, from, to, key);
        var replay = service.createExport(null, from, to, key);
        assertThat(replay.id()).isEqualTo(first.id());
    }

    @Test
    void invalidRangeIsRejected() {
        Instant from = Instant.now(clock).plusSeconds(3600);
        Instant to = Instant.now(clock).minusSeconds(3600);
        assertThatThrownBy(() -> service.createExport(null, from, to, "bad-range"))
                .isInstanceOf(Problems.BadRequest.class);
    }

    private String completedFilePath(String jobId) {
        return "target/test-exports/" + jobId + ".jsonl";
    }
}
