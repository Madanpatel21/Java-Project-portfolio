package com.java700.achain.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.achain.common.TestDb;
import com.java700.achain.common.TestFixtures;
import com.java700.achain.security.LocalUserService;
import com.java700.achain.security.Roles;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end: policy version binding, multi-step dual-control chain, evidence, escalation. */
@SpringBootTest(classes = com.java700.achain.ApprovalChainApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApprovalFlowIT {

    private static final String CHAIN = """
            [{"step":1,"role":"MANAGER","approversRequired":2},
             {"step":2,"role":"DIRECTOR","approversRequired":1},
             {"step":3,"role":"LEGAL_COUNSEL","approversRequired":1}]""";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    LocalUserService localUsers;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    Clock clock;

    private String requester;
    private String manager;
    private String manager2;
    private String director;
    private String legal;
    private String auditor;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "requester", Roles.REQUESTER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "manager", Roles.MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "manager2", Roles.MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "director", Roles.DIRECTOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "legal", Roles.LEGAL_COUNSEL, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        requester = TestFixtures.token(mvc, mapper, "requester");
        manager = TestFixtures.token(mvc, mapper, "manager");
        manager2 = TestFixtures.token(mvc, mapper, "manager2");
        director = TestFixtures.token(mvc, mapper, "director");
        legal = TestFixtures.token(mvc, mapper, "legal");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    private void seedPolicyAndChain() throws Exception {
        mvc.perform(post("/api/v1/policies")
                        .header("Authorization", "Bearer " + legal)
                        .header("Idempotency-Key", "pol-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "policyCode", "CAPEX_APPROVAL", "name", "Capex Approval Policy",
                                "rulesJson", "{\"maxAmount\":100000}"))))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/chains")
                        .header("Authorization", "Bearer " + admin)
                        .header("Idempotency-Key", "chain-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "chainCode", "CAPEX_CHAIN", "name", "Capex Approval Chain",
                                "stepsJson", CHAIN))))
                .andExpect(status().isOk());
    }

    private String createRequest() throws Exception {
        String body = mvc.perform(post("/api/v1/requests")
                        .header("Authorization", "Bearer " + requester)
                        .header("Idempotency-Key", "req-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "chainCode", "CAPEX_CHAIN", "policyCode", "CAPEX_APPROVAL",
                                "subjectType", "CAPEX", "subjectId", "PO-7001",
                                "payload", Map.of("amount", 75000),
                                "dueAt", Instant.now(clock).plus(7, ChronoUnit.DAYS).toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asText();
    }

    @Test
    void fullChainWithDualControlAndPolicyBinding() throws Exception {
        seedPolicyAndChain();
        String requestId = createRequest();
        // policy version is bound at creation
        mvc.perform(get("/api/v1/requests").header("Authorization", "Bearer " + auditor))
                .andExpect(jsonPath("$.items[0].policyVersionId").exists());

        // SoD: requester cannot approve
        mvc.perform(post("/api/v1/requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + requester)
                        .header("Idempotency-Key", "a0")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        // step 1 needs 2 managers: first manager approves (still step 1)
        mvc.perform(post("/api/v1/requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + manager)
                        .header("Idempotency-Key", "a1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(1));
        // same manager cannot decide twice (dual control)
        mvc.perform(post("/api/v1/requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + manager)
                        .header("Idempotency-Key", "a1b")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict());
        // wrong role blocked
        mvc.perform(post("/api/v1/requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + legal)
                        .header("Idempotency-Key", "a1c")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict());

        // second manager completes step 1 -> advance to step 2
        mvc.perform(post("/api/v1/requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + manager2)
                        .header("Idempotency-Key", "a2")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(2));

        // director approves step 2 -> step 3
        mvc.perform(post("/api/v1/requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + director)
                        .header("Idempotency-Key", "a3")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(3));

        // legal approves step 3 -> APPROVED
        mvc.perform(post("/api/v1/requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + legal)
                        .header("Idempotency-Key", "a4")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // evidence: 4 decisions recorded
        mvc.perform(get("/api/v1/requests/" + requestId + "/decisions")
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void rejectionStopsTheChain() throws Exception {
        seedPolicyAndChain();
        String requestId = createRequest();
        mvc.perform(post("/api/v1/requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + manager)
                        .header("Idempotency-Key", "r1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/requests/" + requestId + "/reject")
                        .header("Authorization", "Bearer " + manager2)
                        .header("Idempotency-Key", "r2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Budget not available\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void policyVersionChangeBindsNewRequestsToNewVersion() throws Exception {
        seedPolicyAndChain();
        String firstId = createRequest();
        // activate policy v2
        mvc.perform(post("/api/v1/policies")
                        .header("Authorization", "Bearer " + legal)
                        .header("Idempotency-Key", "pol-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "policyCode", "CAPEX_APPROVAL", "name", "Capex Approval Policy",
                                "rulesJson", "{\"maxAmount\":200000}"))))
                .andExpect(status().isOk());
        String second = mvc.perform(post("/api/v1/requests")
                        .header("Authorization", "Bearer " + requester)
                        .header("Idempotency-Key", "req-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "chainCode", "CAPEX_CHAIN", "policyCode", "CAPEX_APPROVAL",
                                "subjectType", "CAPEX", "subjectId", "PO-7002",
                                "payload", Map.of("amount", 150000),
                                "dueAt", Instant.now(clock).plus(7, ChronoUnit.DAYS).toString()))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String secondId = mapper.readTree(second).get("id").asText();
        String body = mvc.perform(get("/api/v1/requests").header("Authorization", "Bearer " + auditor))
                .andReturn().getResponse().getContentAsString();
        JsonNode items = mapper.readTree(body).get("items");
        String v1 = null;
        String v2 = null;
        for (JsonNode item : items) {
            if (item.get("id").asText().equals(firstId)) {
                v1 = item.get("policyVersionId").asText();
            }
            if (item.get("id").asText().equals(secondId)) {
                v2 = item.get("policyVersionId").asText();
            }
        }
        assertThat(v1).isNotEqualTo(v2);
    }

    @Test
    void escalationExtendsStaleRequests() throws Exception {
        seedPolicyAndChain();
        String requestId = createRequest();
        jdbc.update("update approval_requests set due_at = ? where id = ?",
                java.sql.Timestamp.from(Instant.now(clock).minusSeconds(3600)), requestId);
        mvc.perform(post("/api/v1/escalations").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escalated").value(1));
    }

    @Test
    void malformedChainRejected() throws Exception {
        mvc.perform(post("/api/v1/chains")
                        .header("Authorization", "Bearer " + admin)
                        .header("Idempotency-Key", "bad-chain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "chainCode", "BAD", "name", "Bad",
                                "stepsJson", "[{\"step\":2,\"role\":\"X\",\"approversRequired\":1}]"))))
                .andExpect(status().isBadRequest());
    }
}
