package com.java700.workforce.compliance;

import com.java700.workforce.access.GrantRepository;
import com.java700.workforce.compliance.ViolationRepository;
import static org.assertj.core.api.Assertions.assertThat;

import com.java700.workforce.access.GrantRepository;
import com.java700.workforce.WorkforceComplianceApplication;
import com.java700.workforce.access.Grant;
import com.java700.workforce.compliance.ViolationRepository;
import com.java700.workforce.identity.UserProfile;
import com.java700.workforce.identity.UserProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** Correlation engine over a fixed clock: detects violations, dedupes on rerun, evidences them. */
@SpringBootTest(classes = WorkforceComplianceApplication.class)
@ActiveProfiles("test")
class ComplianceCorrelationIT {

    private static final Instant NOW = Instant.parse("2026-08-18T00:00:00Z");

    @MockitoBean
    Clock clock;

    @Autowired
    CorrelationJob job;
    @Autowired
    UserProfileRepository profiles;
    @Autowired
    GrantRepository grants;
    @Autowired
    ViolationRepository violations;

    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        com.java700.workforce.common.TestDb.clean(jdbc);
        org.mockito.Mockito.when(clock.instant()).thenReturn(NOW);
        org.mockito.Mockito.when(clock.getZone()).thenReturn(java.time.ZoneOffset.UTC);
        String alice = UUID.randomUUID().toString();
        // expired certification + standing ADMIN privilege + overdue recertification + no activity
        profiles.save(new UserProfile(alice, "alice", "alice@corp.example", "HR",
                NOW.minus(10, ChronoUnit.DAYS), NOW));
        try {
            grants.save(new Grant(UUID.randomUUID().toString(), alice, "SYSTEM", "payroll-admin",
                    "[\"ADMIN\"]", NOW.minus(120, ChronoUnit.DAYS), null,
                    NOW.minus(30, ChronoUnit.DAYS)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void correlationDetectsAllRuleFamiliesAndDeduplicatesOnRerun() {
        ComplianceApi.RunResult first = job.run();
        // CERT_EXPIRED + STANDING_PRIVILEGE + RECERT_OVERDUE + INACTIVE_ACCOUNT
        assertThat(first.violationsCreated()).isEqualTo(4);
        assertThat(violations.count()).isEqualTo(4);
        assertThat(violations.findAll().get(0).getEvidenceSeq()).isNotNull();

        ComplianceApi.RunResult second = job.run();
        assertThat(second.violationsCreated()).isZero();
        assertThat(violations.count()).isEqualTo(4);
    }

    @Test
    void remediationFlowClosesLoop() {
        job.run();
        var v = violations.findAll().get(0);
        assertThat(v.getStatus()).isEqualTo(Violation.Status.OPEN);
        v.remediate(NOW, "grant revoked");
        violations.save(v);
        assertThat(v.getStatus()).isEqualTo(Violation.Status.REMEDIATED);
    }
}
