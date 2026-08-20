package com.java700.contracts.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.contracts.common.TestDb;
import com.java700.contracts.common.TestFixtures;
import com.java700.contracts.domain.ObligationRepository;
import com.java700.contracts.security.LocalUserService;
import com.java700.contracts.security.Roles;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
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

/** End-to-end: contract versioning, four-eyes activation, obligation SLA lifecycle. */
@SpringBootTest(classes = com.java700.contracts.ContractLifecycleApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContractFlowIT {

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
    @Autowired
    ObligationRepository obligations;

    private String legal;
    private String cmanager;
    private String finance;
    private String auditor;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "legal", Roles.LEGAL_COUNSEL, null);
        TestFixtures.createUser(localUsers, encoder, clock, "cmanager", Roles.CONTRACT_MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "finance", Roles.FINANCE, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        legal = TestFixtures.token(mvc, mapper, "legal");
        cmanager = TestFixtures.token(mvc, mapper, "cmanager");
        finance = TestFixtures.token(mvc, mapper, "finance");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    private static final String CONTENT = """
            {"clauses":[
              {"number":"1.1","title":"Term","text":"12 months","sensitivity":1},
              {"number":"1.2","title":"Price","text":"100 USD per unit","sensitivity":3}
            ]}""";

    private String createContract(String no) throws Exception {
        String body = mvc.perform(post("/api/v1/contracts")
                        .header("Authorization", "Bearer " + cmanager)
                        .header("Idempotency-Key", "ct-" + no)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "contractNo", no, "title", "Supply Agreement",
                                "counterparty", "Acme Supplies Ltd"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asText();
    }

    private String addVersion(String contractId, String key) throws Exception {
        String body = mvc.perform(post("/api/v1/contracts/" + contractId + "/versions")
                        .header("Authorization", "Bearer " + cmanager)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("contentJson", CONTENT))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return body;
    }

    @Test
    void fourEyesActivationRequiresLegalAndManager() throws Exception {
        String contractId = createContract("CT-1001");
        addVersion(contractId, "v1");

        // one approval is not enough
        mvc.perform(post("/api/v1/contracts/" + contractId + "/activate")
                        .header("Authorization", "Bearer " + cmanager)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/contracts").header("Authorization", "Bearer " + cmanager))
                .andExpect(jsonPath("$.items[0].status").value("DRAFT"));

        // same role cannot decide twice
        mvc.perform(post("/api/v1/contracts/" + contractId + "/activate")
                        .header("Authorization", "Bearer " + cmanager)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict());

        // legal completes the four-eyes
        mvc.perform(post("/api/v1/contracts/" + contractId + "/activate")
                        .header("Authorization", "Bearer " + legal)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/contracts").header("Authorization", "Bearer " + cmanager))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"));
    }

    @Test
    void clauseRedactionByRoleClearance() throws Exception {
        String contractId = createContract("CT-1002");
        addVersion(contractId, "v2");
        // FINANCE (clearance 3) sees everything; AUDITOR (clearance 2) gets redacted price clause
        String financeView = mvc.perform(get("/api/v1/contracts/" + contractId + "/clauses")
                        .header("Authorization", "Bearer " + finance))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(financeView).contains("100 USD per unit");
        assertThat(financeView).doesNotContain("REDACTED");

        String auditorView = mvc.perform(get("/api/v1/contracts/" + contractId + "/clauses")
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(auditorView).contains("REDACTED");
        assertThat(auditorView).doesNotContain("100 USD per unit");
    }

    @Test
    void obligationLifecycleWithSlaScanAndWaiver() throws Exception {
        String contractId = createContract("CT-1003");
        addVersion(contractId, "v3");
        // activate quickly for realism
        mvc.perform(post("/api/v1/contracts/" + contractId + "/activate")
                        .header("Authorization", "Bearer " + cmanager)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"));
        mvc.perform(post("/api/v1/contracts/" + contractId + "/activate")
                        .header("Authorization", "Bearer " + legal)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"));

        // obligation due in 2 days with 30-day window → already inside window
        Instant due = Instant.now(clock).plus(2, ChronoUnit.DAYS);
        String ob = mvc.perform(post("/api/v1/obligations")
                        .header("Authorization", "Bearer " + cmanager)
                        .header("Idempotency-Key", "ob-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "contractId", contractId, "type", "PAYMENT",
                                "title", "Quarterly payment", "dueAt", due.toString(),
                                "windowBeforeDays", 30, "criticality", "HIGH"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String obId = mapper.readTree(ob).get("id").asText();

        // scan → NOTIFIED
        mvc.perform(post("/api/v1/obligations/scan").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/obligations?status=NOTIFIED").header("Authorization", "Bearer " + cmanager))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(obId));

        // waive as manager → 403 (four-eyes); as legal → WAIVED
        mvc.perform(post("/api/v1/obligations/" + obId + "/waive")
                        .header("Authorization", "Bearer " + cmanager)
                        .header("Idempotency-Key", "wv-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/obligations/" + obId + "/waive")
                        .header("Authorization", "Bearer " + legal)
                        .header("Idempotency-Key", "wv-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Payment terms renegotiated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAIVED"));

        // overdue detection: obligation due yesterday
        String ob2 = mvc.perform(post("/api/v1/obligations")
                        .header("Authorization", "Bearer " + cmanager)
                        .header("Idempotency-Key", "ob-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "contractId", contractId, "type", "COMPLIANCE",
                                "title", "Audit certificate", "dueAt",
                                Instant.now(clock).minus(1, ChronoUnit.DAYS).toString(),
                                "windowBeforeDays", 30, "criticality", "MEDIUM"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String ob2Id = mapper.readTree(ob2).get("id").asText();
        mvc.perform(post("/api/v1/obligations/scan").header("Authorization", "Bearer " + admin));
        mvc.perform(get("/api/v1/obligations?status=OVERDUE").header("Authorization", "Bearer " + cmanager))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(ob2Id));
    }

    @Test
    void recurringObligationSpawnsNextInstance() throws Exception {
        String contractId = createContract("CT-1004");
        addVersion(contractId, "v4");
        String ob = mvc.perform(post("/api/v1/obligations")
                        .header("Authorization", "Bearer " + cmanager)
                        .header("Idempotency-Key", "rec-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "contractId", contractId, "type", "PAYMENT",
                                "title", "Monthly rent", "dueAt",
                                Instant.now(clock).plus(5, ChronoUnit.DAYS).toString(),
                                "windowBeforeDays", 30, "repeatIntervalDays", 30,
                                "criticality", "HIGH"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String obId = mapper.readTree(ob).get("id").asText();
        mvc.perform(post("/api/v1/obligations/" + obId + "/complete")
                        .header("Authorization", "Bearer " + cmanager))
                .andExpect(status().isOk());
        assertThat(obligations.count()).isEqualTo(2);
    }

    @Test
    void versionDiffEndpointReportsChanges() throws Exception {
        String contractId = createContract("CT-1005");
        addVersion(contractId, "v5a");
        String v2 = """
                {"clauses":[
                  {"number":"1.1","title":"Term","text":"12 months","sensitivity":1},
                  {"number":"1.2","title":"Price","text":"110 USD per unit","sensitivity":3},
                  {"number":"1.5","title":"Warranty","text":"24 months","sensitivity":2}
                ]}""";
        mvc.perform(post("/api/v1/contracts/" + contractId + "/versions")
                        .header("Authorization", "Bearer " + cmanager)
                        .header("Idempotency-Key", "v5b")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("contentJson", v2))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/contracts/" + contractId + "/diff?v1=1&v2=2")
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.type=='ADDED')]").exists())
                .andExpect(jsonPath("$[?(@.type=='MODIFIED')]").exists());
    }
}
