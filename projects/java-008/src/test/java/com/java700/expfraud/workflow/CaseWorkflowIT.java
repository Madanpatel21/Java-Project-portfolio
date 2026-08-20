package com.java700.expfraud.workflow;

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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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

/** Four-eyes case workflow end to end, including the manager approval block for high risk. */
@SpringBootTest(classes = com.java700.expfraud.ExpenseFraudApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CaseWorkflowIT {

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
    private String investigator2;
    private String auditor;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "employee", Roles.EMPLOYEE, null);
        TestFixtures.createUser(localUsers, encoder, clock, "manager", Roles.MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "investigator",
                Roles.FRAUD_INVESTIGATOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "investigator2",
                Roles.FRAUD_INVESTIGATOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        employee = TestFixtures.token(mvc, mapper, "employee");
        manager = TestFixtures.token(mvc, mapper, "manager");
        investigator = TestFixtures.token(mvc, mapper, "investigator");
        investigator2 = TestFixtures.token(mvc, mapper, "investigator2");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
    }

    /** High-risk mileage: cap violation + weekend + duplicate cluster = 85 points -> auto case. */
    private JsonNode submitHighRiskClaim(String merchantPrefix, String receipt) throws Exception {
        LocalDate saturday = LocalDate.now(clock)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        LocalDate friday = saturday.minusDays(1);
        TestFixtures.submitClaim(mvc, mapper, employee, "emp-101", "Ravi Kumar", "ENGINEERING",
                "MILEAGE", "350.00", merchantPrefix, friday.toString(), receipt + "a", null);
        return TestFixtures.submitClaim(mvc, mapper, employee, "emp-101", "Ravi Kumar",
                "ENGINEERING", "MILEAGE", "350.00", merchantPrefix, saturday.toString(),
                receipt + "b", null);
    }

    private String openCaseIdOf(String claimId) throws Exception {
        String body = mvc.perform(get("/api/v1/cases")
                        .header("Authorization", "Bearer " + investigator))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode node : mapper.readTree(body)) {
            if (claimId.equals(node.get("claimId").asText())) {
                return node.get("id").asText();
            }
        }
        throw new AssertionError("no open case for claim " + claimId);
    }

    @Test
    void highRiskClaimIsLockedFromManagerAndNeedsTwoInvestigators() throws Exception {
        JsonNode claim = submitHighRiskClaim("Shell Fuel", "r-501");
        assertThat(claim.get("status").asText()).isEqualTo("UNDER_REVIEW");
        assertThat(claim.get("riskTier").asText()).isEqualTo("HIGH");
        assertThat(claim.get("riskScore").asInt()).isGreaterThanOrEqualTo(65);
        String claimId = claim.get("id").asText();
        String caseId = openCaseIdOf(claimId);

        // Manager is blocked: high-risk claims must go through the case workflow.
        mvc.perform(post("/api/v1/claims/" + claimId + "/approve")
                        .header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"note\":\"ok\"}"))
                .andExpect(status().isConflict());

        // First investigator records a fraud recommendation.
        mvc.perform(post("/api/v1/cases/" + caseId + "/review")
                        .header("Authorization", "Bearer " + investigator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recommendation\":\"RECOMMEND_FRAUD\",\"note\":\"receipts look fabricated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEWED"));

        // Four-eyes: the same investigator cannot make the final decision.
        mvc.perform(post("/api/v1/cases/" + caseId + "/decide")
                        .header("Authorization", "Bearer " + investigator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"CONFIRM_FRAUD\",\"note\":\"confirmed\"}"))
                .andExpect(status().isConflict());

        // A second, different investigator confirms the fraud.
        mvc.perform(post("/api/v1/cases/" + caseId + "/decide")
                        .header("Authorization", "Bearer " + investigator2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"CONFIRM_FRAUD\",\"note\":\"duplicate mileage confirmed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED_FRAUD"))
                .andExpect(jsonPath("$.decision").value("CONFIRM_FRAUD"));

        // The claim follows the case decision.
        mvc.perform(get("/api/v1/claims/" + claimId)
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED_FRAUD"));

        // Decisions cannot be repeated.
        mvc.perform(post("/api/v1/cases/" + caseId + "/review")
                        .header("Authorization", "Bearer " + investigator2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recommendation\":\"RECOMMEND_FRAUD\",\"note\":\"x\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void clearedCaseApprovesTheClaim() throws Exception {
        JsonNode claim = submitHighRiskClaim("BP Fuel", "r-502");
        String caseId = openCaseIdOf(claim.get("id").asText());

        mvc.perform(post("/api/v1/cases/" + caseId + "/review")
                        .header("Authorization", "Bearer " + investigator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recommendation\":\"RECOMMEND_CLEAR\",\"note\":\"weekend client visit documented\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/cases/" + caseId + "/decide")
                        .header("Authorization", "Bearer " + investigator2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"CLEAR\",\"note\":\"legitimate trip\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLEARED"));

        mvc.perform(get("/api/v1/claims/" + claim.get("id").asText())
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void invalidTransitionsAreRejected() throws Exception {
        JsonNode claim = submitHighRiskClaim("Esso Fuel", "r-503");
        String caseId = openCaseIdOf(claim.get("id").asText());

        // Decide before review.
        mvc.perform(post("/api/v1/cases/" + caseId + "/decide")
                        .header("Authorization", "Bearer " + investigator2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"CLEAR\",\"note\":\"x\"}"))
                .andExpect(status().isConflict());
        // Invalid decision value.
        mvc.perform(post("/api/v1/cases/" + caseId + "/review")
                        .header("Authorization", "Bearer " + investigator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recommendation\":\"BURN_IT\",\"note\":\"x\"}"))
                .andExpect(status().isBadRequest());
        // Unknown case.
        mvc.perform(post("/api/v1/cases/does-not-exist/review")
                        .header("Authorization", "Bearer " + investigator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recommendation\":\"RECOMMEND_CLEAR\",\"note\":\"x\"}"))
                .andExpect(status().isNotFound());
    }
}
