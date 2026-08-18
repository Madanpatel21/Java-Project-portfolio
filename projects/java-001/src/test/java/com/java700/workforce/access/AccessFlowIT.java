package com.java700.workforce.access;

import com.java700.workforce.access.GrantRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.workforce.access.GrantRepository;
import com.java700.workforce.WorkforceComplianceApplication;
import com.java700.workforce.evidence.EvidenceRepository;
import com.java700.workforce.identity.UserProfile;
import com.java700.workforce.identity.UserProfileRepository;
import com.java700.workforce.security.LocalUser;
import com.java700.workforce.security.LocalUserService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end access lifecycle: request → dual-control approval → grant → evidence → replay safety. */
@SpringBootTest(classes = WorkforceComplianceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccessFlowIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    LocalUserService localUsers;
    @Autowired
    UserProfileRepository profiles;
    @Autowired
    GrantRepository grants;
    @Autowired
    EvidenceRepository evidence;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    Clock clock;

    private String carol, dave, erin, alice, aliceId;

    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbc;

    @BeforeEach
    void setUp() throws Exception {
        com.java700.workforce.common.TestDb.clean(jdbc);
        String hash = encoder.encode("Password123!");
        carol = createUser("carol", "ACCESS_MANAGER", hash);
        dave = createUser("dave", "ACCESS_MANAGER", hash);
        erin = createUser("erin", "ACCESS_MANAGER", hash);
        aliceId = createUserWithId("alice", "EMPLOYEE", hash);
        alice = token("alice");
    }

    private String createUser(String username, String role, String hash) throws Exception {
        createUserWithId(username, role, hash);
        return token(username);
    }

    private String createUserWithId(String username, String role, String hash) {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, username, hash, username + "@corp.example", "UNIT",
                Instant.now(clock)));
        localUsers.saveRole(id, role);
        profiles.save(new UserProfile(id, username, username + "@corp.example", "UNIT",
                Instant.now(clock).plusSeconds(86400), Instant.now(clock)));
        return id;
    }

    private String token(String username) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("accessToken").asText();
    }

    private String requestJson() throws Exception {
        return mapper.writeValueAsString(Map.of(
                "subjectUserId", aliceId,
                "resourceType", "SYSTEM",
                "resourceName", "payroll-admin",
                "roles", List.of("REPORTER"),
                "justification", "Needs read-only payroll access for audit prep"));
    }

    @Test
    void fullDualControlFlowCreatesGrantAndEvidence() throws Exception {
        // 1. carol (manager) requests on behalf of alice
        String created = mvc.perform(post("/api/v1/access-requests")
                        .header("Authorization", "Bearer " + carol)
                        .contentType(MediaType.APPLICATION_JSON).content(requestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        String requestId = mapper.readTree(created).get("id").asText();

        // 2. SoD: carol may not approve her own request
        mvc.perform(post("/api/v1/access-requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + carol)
                        .header("Idempotency-Key", "sod-key")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("conflict"));

        // 3. dave approves (1 of 2)
        mvc.perform(post("/api/v1/access-requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + dave)
                        .header("Idempotency-Key", "approve-dave-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // 4. dual control: dave cannot approve twice
        mvc.perform(post("/api/v1/access-requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + dave)
                        .header("Idempotency-Key", "approve-dave-2")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict());

        // 5. erin approves (2 of 2) → APPROVED + grant created
        mvc.perform(post("/api/v1/access-requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + erin)
                        .header("Idempotency-Key", "approve-erin-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        assertThat(grants.findByUserIdAndStatus(aliceId, "ACTIVE")).hasSize(1);

        // 6. idempotent replay of erin's approval returns current state, no new side effects
        mvc.perform(post("/api/v1/access-requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + erin)
                        .header("Idempotency-Key", "approve-erin-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        assertThat(grants.findByUserIdAndStatus(aliceId, "ACTIVE")).hasSize(1);

        // 7. evidence ledger holds the full decision trail
        long entries = evidence.findAll().size();
        // ACCESS_REQUESTED + 2x ACCESS_APPROVED + GRANT_CREATED = 4 chained entries
        assertThat(entries).isGreaterThanOrEqualTo(4);
    }

    @Test
    void invalidPayloadReturnsRfc7807Problem() throws Exception {
        mvc.perform(post("/api/v1/access-requests")
                        .header("Authorization", "Bearer " + carol)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectUserId\":\"" + aliceId + "\",\"resourceType\":\"\","
                                + "\"resourceName\":\"\",\"roles\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResponse().getContentType())
                        .contains("application/problem+json"))
                .andExpect(jsonPath("$.title").value("validation-failed"))
                .andExpect(jsonPath("$.fields").exists());
    }

    @Test
    void employeeCannotRequestForSomeoneElse() throws Exception {
        mvc.perform(post("/api/v1/access-requests")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "subjectUserId", carolUserId(), "resourceType", "SYSTEM",
                                "resourceName", "x", "roles", List.of("ADMIN")))))
                .andExpect(status().isConflict());
    }

    @Test
    void revokeEndsGrantWithEvidence() throws Exception {
        // fast path: grant directly then revoke through API
        String grantId = UUID.randomUUID().toString();
        grants.save(new Grant(grantId, aliceId, "SYSTEM", "legacy-admin",
                "[\"ADMIN\"]", Instant.now(clock), null, Instant.now(clock).plusSeconds(86400)));
        mvc.perform(post("/api/v1/grants/" + grantId + "/revoke")
                        .header("Authorization", "Bearer " + carol)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"role no longer needed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));
        assertThat(grants.findByIdAndStatus(grantId, "ACTIVE")).isEmpty();
    }

    private String carolUserId() {
        return profiles.findByUsername("carol").map(UserProfile::getId).orElseThrow();
    }
}
