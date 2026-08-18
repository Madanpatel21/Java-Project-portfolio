package com.java700.workforce.compliance;

import com.java700.workforce.compliance.ViolationRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.java700.workforce.common.api.Problems;
import com.java700.workforce.common.audit.AuditLogService;
import com.java700.workforce.compliance.ViolationRepository;
import com.java700.workforce.evidence.EvidenceEntry;
import com.java700.workforce.evidence.EvidenceService;
import com.java700.workforce.messaging.DomainEventBus;
import com.java700.workforce.observability.Metrics;
import com.java700.workforce.policy.RuleEngine;
import com.java700.workforce.policy.RuleType;
import com.java700.workforce.policy.ViolationCandidate;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ViolationServiceTest {

    private ViolationRepository repository;
    private EvidenceService evidence;
    private ViolationService service;

    @BeforeEach
    void setUp() {
        repository = mock(ViolationRepository.class);
        evidence = mock(EvidenceService.class);
        when(evidence.append(any(), any(), any(), any(), any()))
                .thenReturn(new EvidenceEntry(7L, "VIOLATION", "v1", "VIOLATION_DETECTED", "engine",
                        "{}", "0".repeat(64), "a".repeat(64), Instant.now()));
        service = new ViolationService(repository, evidence, mock(AuditLogService.class),
                mock(DomainEventBus.class), mock(Metrics.class), Clock.systemUTC());
    }

    private ViolationCandidate candidate() {
        return new ViolationCandidate("u1", "ACCESS_GOVERNANCE", RuleType.CERT_EXPIRED,
                RuleEngine.Severity.HIGH, "admin-console", "Certification expired");
    }

    @Test
    void detectsNewViolationWithEvidenceLinkage() {
        when(repository.findByUserIdAndPolicyCodeAndRuleTypeAndStatusIn(
                "u1", "ACCESS_GOVERNANCE", "CERT_EXPIRED", List.of("OPEN", "ACKNOWLEDGED")))
                .thenReturn(Optional.empty());
        Violation v = service.detect(candidate());
        assertThat(v).isNotNull();
        assertThat(v.getStatus()).isEqualTo(Violation.Status.OPEN);
        assertThat(v.getEvidenceSeq()).isEqualTo(7L);
    }

    @Test
    void deduplicatesAgainstOpenViolations() {
        when(repository.findByUserIdAndPolicyCodeAndRuleTypeAndStatusIn(
                "u1", "ACCESS_GOVERNANCE", "CERT_EXPIRED", List.of("OPEN", "ACKNOWLEDGED")))
                .thenReturn(Optional.of(new Violation("v1", "u1", "ACCESS_GOVERNANCE", "CERT_EXPIRED",
                        "HIGH", "Certification expired", Instant.now())));
        assertThat(service.detect(candidate())).isNull();
    }

    @Test
    void lifecycleTransitionsAreControlled() {
        Violation open = new Violation("v1", "u1", "ACCESS_GOVERNANCE", "CERT_EXPIRED", "HIGH",
                "desc", Instant.now());
        when(repository.findById("v1")).thenReturn(Optional.of(open));
        assertThatThrownBy(() -> service.close("v1")).isInstanceOf(Problems.Conflict.class);
        open.acknowledge(Instant.now());
        assertThatThrownBy(() -> service.close("v1")).isInstanceOf(Problems.Conflict.class);
        open.remediate(Instant.now(), "revoked grant");
        assertThat(service.close("v1").status()).isEqualTo(ComplianceApi.ViolationView.from(open).status());
    }
}
