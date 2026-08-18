package com.java700.workforce.evidence;

import static org.assertj.core.api.Assertions.assertThat;import com.java700.workforce.WorkforceComplianceApplication;


import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Full-stack evidence integrity: appends, verification, tamper detection, health signal. */
@SpringBootTest(classes = WorkforceComplianceApplication.class)
@ActiveProfiles("test")
class EvidenceChainIT {

    @Autowired
    EvidenceService service;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    EvidenceHealthIndicator health;

    @BeforeEach
    void reset() {
        jdbc.update("delete from evidence_entry");
        jdbc.update("alter table evidence_entry alter column seq restart with 1");
    }

    private void appendSome(int n) {
        for (int i = 0; i < n; i++) {
            service.append("TEST", "agg-1", "EVENT_" + i, "tester",
                    Map.of("i", i, "note", "entry " + i));
        }
    }

    @Test
    void appendAndVerifyCleanChain() {
        appendSome(3);
        assertThat(service.count()).isEqualTo(3);
        HashChain.Verification v = service.verifyChain();
        assertThat(v.valid()).isTrue();
        assertThat(v.entriesChecked()).isEqualTo(3);
    }

    @Test
    void tamperedPayloadIsDetectedAndHealthGoesDown() {
        appendSome(5);
        jdbc.update("update evidence_entry set payload = '{\"tampered\":true}' where seq = 3");
        HashChain.Verification v = service.verifyChain();
        assertThat(v.valid()).isFalse();
        assertThat(v.brokenSeq()).isEqualTo(3L);
        assertThat(health.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void sequenceGapIsDetected() {
        appendSome(4);
        jdbc.update("update evidence_entry set seq = 99 where seq = 4");
        HashChain.Verification v = service.verifyChain();
        assertThat(v.valid()).isFalse();
        assertThat(v.brokenSeq()).isEqualTo(99L);
    }
}
