package com.java700.expfraud.claims;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.expfraud.common.TestDb;
import com.java700.expfraud.common.TestFixtures;
import com.java700.expfraud.security.LocalUserService;
import com.java700.expfraud.security.Roles;
import java.time.Clock;
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

/** Claim guards: idempotency, validation-first errors, masking and role boundaries. */
@SpringBootTest(classes = com.java700.expfraud.ExpenseFraudApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimGuardIT {

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

    private String employee;
    private String manager;
    private String investigator;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "employee", Roles.EMPLOYEE, null);
        TestFixtures.createUser(localUsers, encoder, clock, "manager", Roles.MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "investigator",
                Roles.FRAUD_INVESTIGATOR, null);
        employee = TestFixtures.token(mvc, mapper, "employee");
        manager = TestFixtures.token(mvc, mapper, "manager");
        investigator = TestFixtures.token(mvc, mapper, "investigator");
    }

    private JsonNode submitClean(String idem) throws Exception {
        return TestFixtures.submitClaim(mvc, mapper, employee, "emp-202", "Priya Patel", "SALES",
                "MEALS", "44.25", "Green Leaf", "2026-08-05", "r-601", idem);
    }

    @Test
    void idempotentSubmissionReturnsTheSameClaim() throws Exception {
        JsonNode first = submitClean("idem-guard-1");
        JsonNode second = submitClean("idem-guard-1");
        assertThat(first.get("id").asText()).isEqualTo(second.get("id").asText());
        assertThat(jdbc.queryForObject("select count(*) from expense_claims", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void managerApprovesAndRejectsCleanClaims() throws Exception {
        JsonNode approved = submitClean("idem-guard-2");
        mvc.perform(post("/api/v1/claims/" + approved.get("id").asText() + "/approve")
                        .header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"note\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        JsonNode rejected = submitClean("idem-guard-3");
        mvc.perform(post("/api/v1/claims/" + rejected.get("id").asText() + "/reject")
                        .header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"note\":\"no policy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        // Employees cannot decide claims.
        JsonNode third = submitClean("idem-guard-4");
        mvc.perform(post("/api/v1/claims/" + third.get("id").asText() + "/approve")
                        .header("Authorization", "Bearer " + employee)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"note\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validationErrorsWinBeforePersistence() throws Exception {
        String base = "{\"employeeId\":\"emp-9\",\"employeeName\":\"X\",\"department\":\"SALES\","
                + "\"category\":\"MEALS\",\"amount\":10,\"currency\":\"USD\",\"merchant\":\"M\","
                + "\"expenseDate\":\"2026-08-05\",\"receiptRef\":\"r\"}";
        mvc.perform(post("/api/v1/claims").header("Authorization", "Bearer " + employee)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(base.replace("\"category\":\"MEALS\"", "\"category\":\"BRIBES\"")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/claims").header("Authorization", "Bearer " + employee)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(base.replace("\"amount\":10", "\"amount\":-5")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/claims").header("Authorization", "Bearer " + employee)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(base.replace("\"expenseDate\":\"2026-08-05\"",
                                "\"expenseDate\":\"2099-01-01\"")))
                .andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("select count(*) from expense_claims", Integer.class))
                .isZero();
    }

    @Test
    void employeeIdentityIsMaskedForNonPrivilegedReaders() throws Exception {
        JsonNode claim = submitClean("idem-guard-5");
        String claimId = claim.get("id").asText();

        String auditorView = mvc.perform(get("/api/v1/claims/" + claimId)
                        .header("Authorization", "Bearer " + investigator))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(auditorView).get("employeeName").asText())
                .isEqualTo("Priya Patel");

        String employeeView = mvc.perform(get("/api/v1/claims/" + claimId)
                        .header("Authorization", "Bearer " + employee))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String masked = mapper.readTree(employeeView).get("employeeName").asText();
        assertThat(masked).doesNotContain("Priya Patel");
        assertThat(masked).contains("•");

        // The review queue is investigator-only.
        mvc.perform(get("/api/v1/claims/queue")
                        .header("Authorization", "Bearer " + employee))
                .andExpect(status().isForbidden());
    }
}
