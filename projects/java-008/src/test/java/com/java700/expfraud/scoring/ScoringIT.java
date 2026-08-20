package com.java700.expfraud.scoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import java.util.ArrayList;
import java.util.List;
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

/** Scoring pipeline: policy rules, weekend mileage, round amounts, duplicates, peer outliers. */
@SpringBootTest(classes = com.java700.expfraud.ExpenseFraudApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScoringIT {

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
    private String auditor;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "employee", Roles.EMPLOYEE, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        employee = TestFixtures.token(mvc, mapper, "employee");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    private JsonNode submit(String category, String amount, String merchant, String date,
                            String receipt) throws Exception {
        return TestFixtures.submitClaim(mvc, mapper, employee, "emp-101", "Ravi Kumar",
                "ENGINEERING", category, amount, merchant, date, receipt, null);
    }

    private List<String> reasonCodes(JsonNode claim) {
        List<String> codes = new ArrayList<>();
        claim.get("reasons").forEach(reason -> codes.add(reason.get("code").asText()));
        return codes;
    }

    @Test
    void overCapMealIsFlaggedWithViolationAndExplainableReason() throws Exception {
        JsonNode claim = submit("MEALS", "121.35", "Spice Garden", "2026-08-10", "r-101");
        assertThat(claim.get("status").asText()).isEqualTo("SCORED");
        assertThat(claim.get("riskScore").asInt()).isEqualTo(25);
        assertThat(reasonCodes(claim)).contains("MEALS-CAP");
        assertThat(claim.get("reasons").get(0).get("severity").asText()).isEqualTo("VIOLATION");
    }

    @Test
    void weekendMileageIsFlaggedAsAnomaly() throws Exception {
        LocalDate saturday = LocalDate.now(clock)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        JsonNode claim = submit("MILEAGE", "245.75", "Shell Fuel", saturday.toString(), "r-102");
        assertThat(claim.get("status").asText()).isEqualTo("SCORED");
        assertThat(reasonCodes(claim)).contains("WEEKEND-MILEAGE");
        assertThat(claim.get("riskScore").asInt()).isEqualTo(20);
    }

    @Test
    void roundAmountIsFlaggedAsWarning() throws Exception {
        JsonNode claim = submit("SUPPLIES", "500.00", "OfficeMart", "2026-08-11", "r-103");
        assertThat(reasonCodes(claim)).contains("ROUND-AMOUNT");
        assertThat(claim.get("riskScore").asInt()).isEqualTo(10);
        assertThat(claim.get("reasons").get(0).get("severity").asText()).isEqualTo("WARNING");
    }

    @Test
    void atmBlockerOpensCaseAndLocksManagerApproval() throws Exception {
        JsonNode claim = submit("OTHER", "200.00", "ATM Cash Withdrawal", "2026-08-12", "r-104");
        assertThat(claim.get("status").asText()).isEqualTo("UNDER_REVIEW");
        assertThat(reasonCodes(claim)).contains("ATM-BLOCKER");
        String claimId = claim.get("id").asText();
        mvc.perform(post("/api/v1/claims/" + claimId + "/approve")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"note\":\"n\"}"))
                .andExpect(status().isConflict());
        String cases = mvc.perform(get("/api/v1/cases")
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode caseList = mapper.readTree(cases);
        assertThat(caseList).hasSize(1);
        assertThat(caseList.get(0).get("riskScore").asInt()).isEqualTo(claim.get("riskScore").asInt());
    }

    @Test
    void duplicateClaimsClusterIntoEvidenceGroup() throws Exception {
        LocalDate today = LocalDate.now(clock);
        JsonNode first = submit("MEALS", "61.75", "Taj Kitchen", today.minusDays(2).toString(),
                "r-201");
        JsonNode second = submit("MEALS", "61.75", "Taj Kitchen", today.minusDays(1).toString(),
                "r-202");
        assertThat(first.get("riskScore").asInt()).isEqualTo(0);
        assertThat(reasonCodes(second)).contains("DUPLICATE-CLUSTER");
        assertThat(second.get("riskScore").asInt()).isEqualTo(30);

        String groups = mvc.perform(get("/api/v1/claims/duplicate-groups")
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode groupList = mapper.readTree(groups);
        assertThat(groupList).hasSize(1);
        assertThat(groupList.get(0).get("size").asInt()).isEqualTo(2);
        assertThat(groupList.get(0).get("claimNos")).hasSize(2);
    }

    @Test
    void peerOutlierIsFlaggedAgainstDepartmentBaseline() throws Exception {
        String[] amounts = {"42.55", "47.25", "51.05", "55.75", "62.30", "68.40"};
        for (int i = 0; i < amounts.length; i++) {
            submit("MEALS", amounts[i], "Canteen " + i, "2026-07-" + (10 + i), "r-3" + i);
        }
        String baselines = mvc.perform(post("/api/v1/admin/baselines/recompute")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode baselineList = mapper.readTree(baselines);
        assertThat(baselineList).isNotEmpty();
        JsonNode baseline = baselineList.get(0);
        assertThat(baseline.get("sampleCount").asInt()).isEqualTo(6);

        JsonNode outlier = submit("MEALS", "247.35", "Spice Garden", "2026-08-14", "r-399");
        assertThat(reasonCodes(outlier)).contains("PEER-OUTLIER");
        assertThat(outlier.get("riskScore").asInt()).isEqualTo(55);
    }
}
