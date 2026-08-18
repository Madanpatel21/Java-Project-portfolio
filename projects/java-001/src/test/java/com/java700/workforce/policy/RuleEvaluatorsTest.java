package com.java700.workforce.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.workforce.access.Grant;
import com.java700.workforce.identity.UserProfile;
import com.java700.workforce.policy.rules.CertExpiryEvaluator;
import com.java700.workforce.policy.rules.InactiveAccountEvaluator;
import com.java700.workforce.policy.rules.RecertOverdueEvaluator;
import com.java700.workforce.policy.rules.SodConflictEvaluator;
import com.java700.workforce.policy.rules.StandingPrivilegeEvaluator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RuleEvaluatorsTest {

    private static final Instant NOW = Instant.parse("2026-08-18T00:00:00Z");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static RuleEngine engine;

    @BeforeAll
    static void setUp() {
        engine = new RuleEngine(MAPPER, List.of(
                new SodConflictEvaluator(), new CertExpiryEvaluator(), new RecertOverdueEvaluator(),
                new StandingPrivilegeEvaluator(), new InactiveAccountEvaluator()));
    }

    private static Grant grant(String userId, String resource, List<String> roles, Instant grantedAt,
                               Instant recertDue) {
        try {
            return new Grant(UUID.randomUUID().toString(), userId, "SYSTEM", resource,
                    MAPPER.writeValueAsString(roles), grantedAt, null, recertDue);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static RuleEngine.RuleDefinition rule(String rulesJson, RuleType type) {
        return engine.parse("ACCESS_GOVERNANCE", rulesJson).stream()
                .filter(r -> r.type() == type).findFirst().orElseThrow();
    }

    @Test
    void sodConflictDetectsConflictingRolesOnSameResource() {
        var def = rule("""
                [{"type":"SOD_CONFLICT","severity":"HIGH",
                  "params":{"conflictPairs":[["APPROVER","REQUESTER"],["ADMIN","AUDITOR"]]}}]""",
                RuleType.SOD_CONFLICT);
        var ctx = new RuleEvaluator.EvaluationContext(NOW,
                List.of(grant("u1", "res-a", List.of("APPROVER", "REQUESTER"), NOW, null),
                        grant("u2", "res-a", List.of("DEVELOPER"), NOW, null)),
                Map.of(), Map.of());
        var hits = engine.evaluatorFor(RuleType.SOD_CONFLICT).evaluate(def, ctx);
        assertThat(hits).hasSize(1);
        assertThat(hits.get(0).userId()).isEqualTo("u1");
        assertThat(hits.get(0).severity()).isEqualTo(RuleEngine.Severity.HIGH);
    }

    @Test
    void certExpiryDetectsMissingAndExpiredCertifications() {
        var def = rule("""
                [{"type":"CERT_EXPIRED","severity":"HIGH","params":{"certRequiredRoles":["ADMIN"]}}]""",
                RuleType.CERT_EXPIRED);
        UserProfile noCert = new UserProfile("u1", "u1", "u1@x.io", "HR", null, NOW);
        UserProfile expired = new UserProfile("u2", "u2", "u2@x.io", "HR",
                NOW.minus(1, ChronoUnit.DAYS), NOW);
        UserProfile valid = new UserProfile("u3", "u3", "u3@x.io", "HR",
                NOW.plus(30, ChronoUnit.DAYS), NOW);
        var ctx = new RuleEvaluator.EvaluationContext(NOW,
                List.of(grant("u1", "admin-console", List.of("ADMIN"), NOW, null),
                        grant("u2", "admin-console", List.of("ADMIN"), NOW, null),
                        grant("u3", "admin-console", List.of("ADMIN"), NOW, null)),
                Map.of("u1", noCert, "u2", expired, "u3", valid), Map.of());
        var hits = engine.evaluatorFor(RuleType.CERT_EXPIRED).evaluate(def, ctx);
        assertThat(hits).extracting(ViolationCandidate::userId).containsExactlyInAnyOrder("u1", "u2");
    }

    @Test
    void recertOverdueDetectsPastDueGrants() {
        var def = rule("""
                [{"type":"RECERT_OVERDUE","severity":"MEDIUM","params":{"intervalDays":90}}]""",
                RuleType.RECERT_OVERDUE);
        var ctx = new RuleEvaluator.EvaluationContext(NOW,
                List.of(grant("u1", "res", List.of("DEV"), NOW, NOW.minus(1, ChronoUnit.DAYS)),
                        grant("u2", "res", List.of("DEV"), NOW, NOW.plus(10, ChronoUnit.DAYS))),
                Map.of(), Map.of());
        var hits = engine.evaluatorFor(RuleType.RECERT_OVERDUE).evaluate(def, ctx);
        assertThat(hits).hasSize(1);
        assertThat(hits.get(0).userId()).isEqualTo("u1");
    }

    @Test
    void standingPrivilegeDetectsOveragedAdminAccess() {
        var def = rule("""
                [{"type":"STANDING_PRIVILEGE","severity":"HIGH",
                  "params":{"privilegedRoles":["ADMIN"],"maxDays":90}}]""",
                RuleType.STANDING_PRIVILEGE);
        var ctx = new RuleEvaluator.EvaluationContext(NOW,
                List.of(grant("u1", "admin", List.of("ADMIN"), NOW.minus(100, ChronoUnit.DAYS), null),
                        grant("u2", "admin", List.of("ADMIN"), NOW.minus(10, ChronoUnit.DAYS), null)),
                Map.of(), Map.of());
        var hits = engine.evaluatorFor(RuleType.STANDING_PRIVILEGE).evaluate(def, ctx);
        assertThat(hits).hasSize(1);
        assertThat(hits.get(0).userId()).isEqualTo("u1");
    }

    @Test
    void inactiveAccountDetectsNoRecentActivity() {
        var def = rule("""
                [{"type":"INACTIVE_ACCOUNT","severity":"MEDIUM","params":{"inactiveDays":60}}]""",
                RuleType.INACTIVE_ACCOUNT);
        var ctx = new RuleEvaluator.EvaluationContext(NOW,
                List.of(grant("u1", "res", List.of("DEV"), NOW, null),
                        grant("u2", "res", List.of("DEV"), NOW, null)),
                Map.of(),
                Map.of("u2", NOW.minus(5, ChronoUnit.DAYS)));
        var hits = engine.evaluatorFor(RuleType.INACTIVE_ACCOUNT).evaluate(def, ctx);
        assertThat(hits).hasSize(1);
        assertThat(hits.get(0).userId()).isEqualTo("u1");
    }
}
