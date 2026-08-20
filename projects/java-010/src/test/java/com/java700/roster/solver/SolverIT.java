package com.java700.roster.solver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.roster.common.TestDb;
import com.java700.roster.common.TestFixtures;
import com.java700.roster.security.LocalUserService;
import com.java700.roster.security.Roles;
import java.time.Clock;
import java.time.LocalDate;
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

/** Timefold optimization: feasible staffing, full coverage, skill matching, explanations. */
@SpringBootTest(classes = com.java700.roster.RosterOptimizerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SolverIT {

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

    private String manager;
    private String auditor;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "manager", Roles.MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        manager = TestFixtures.token(mvc, mapper, "manager");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
    }

    private void createEmployee(String empNo, String name, String skills, int maxWeekly)
            throws Exception {
        String body = String.format("{\"empNo\":\"%s\",\"name\":\"%s\",\"department\":\"OPS\","
                        + "\"skills\":\"%s\",\"employmentType\":\"FULL_TIME\","
                        + "\"maxWeeklyHours\":%d}",
                empNo, name, skills, maxWeekly);
        mvc.perform(post("/api/v1/employees").header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    private String createRoster() throws Exception {
        LocalDate start = LocalDate.now(clock).plusDays(3);
        String body = String.format("{\"name\":\"Week 34 OPS\",\"department\":\"OPS\","
                        + "\"startDate\":\"%s\",\"days\":7,"
                        + "\"demand\":[{\"shiftType\":\"MORNING\",\"startHour\":6,"
                        + "\"durationHours\":8,\"requiredSkill\":\"NURSE\",\"headcount\":1},"
                        + "{\"shiftType\":\"AFTERNOON\",\"startHour\":14,"
                        + "\"durationHours\":8,\"requiredSkill\":\"CARE\",\"headcount\":1},"
                        + "{\"shiftType\":\"NIGHT\",\"startHour\":22,"
                        + "\"durationHours\":8,\"requiredSkill\":\"NURSE\",\"headcount\":1}]}",
                start);
        String response = mvc.perform(post("/api/v1/rosters")
                        .header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asText();
    }

    @Test
    void solverStaffsEverySlotFeasiblyWithSkillMatches() throws Exception {
        createEmployee("EMP-201", "Ana", "NURSE,CARE", 40);
        createEmployee("EMP-202", "Bob", "NURSE", 40);
        createEmployee("EMP-203", "Carla", "CARE,DRIVER", 40);
        createEmployee("EMP-204", "Dev", "NURSE,DRIVER", 40);
        createEmployee("EMP-205", "Eva", "CARE", 40);
        String rosterId = createRoster();

        String optimizeBody = mvc.perform(post("/api/v1/rosters/" + rosterId + "/optimize")
                        .header("Authorization", "Bearer " + manager))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode result = mapper.readTree(optimizeBody);
        assertThat(result.get("feasible").asBoolean()).isTrue();
        assertThat(result.get("totalShifts").asInt()).isEqualTo(21);
        assertThat(result.get("assigned").asInt()).isEqualTo(21);
        assertThat(result.get("score").asText()).contains("0hard");

        // Every assignment must hold the required skill.
        String shiftsBody = mvc.perform(get("/api/v1/rosters/" + rosterId + "/shifts")
                        .header("Authorization", "Bearer " + manager))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode shift : mapper.readTree(shiftsBody)) {
            assertThat(shift.get("employeeEmpNo").asText()).isNotNull();
            String skills = skillsOf(shift.get("employeeEmpNo").asText());
            assertThat(skills).contains(shift.get("requiredSkill").asText());
        }

        // Explanation exposes the constraint matches for auditors.
        String explainBody = mvc.perform(get("/api/v1/rosters/" + rosterId + "/explain")
                        .header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode explain = mapper.readTree(explainBody);
        assertThat(explain.get("feasible").asBoolean()).isTrue();
        assertThat(explain.get("matches")).isNotEmpty();
        boolean sawFairness = false;
        for (JsonNode match : explain.get("matches")) {
            if (match.get("constraint").asText().contains("Fairness")) {
                sawFairness = true;
            }
        }
        assertThat(sawFairness).isTrue();
    }

    @Test
    void publishRequiresFullCoverageAndFeasibility() throws Exception {
        createEmployee("EMP-301", "Ana", "NURSE,CARE", 40);
        createEmployee("EMP-302", "Bob", "NURSE", 40);
        createEmployee("EMP-303", "Carla", "CARE", 40);
        createEmployee("EMP-304", "Dev", "NURSE,DRIVER", 40);
        createEmployee("EMP-305", "Eva", "CARE", 40);
        String rosterId = createRoster();

        mvc.perform(post("/api/v1/rosters/" + rosterId + "/publish")
                        .header("Authorization", "Bearer " + manager))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/v1/rosters/" + rosterId + "/optimize")
                        .header("Authorization", "Bearer " + manager))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/rosters/" + rosterId + "/publish")
                        .header("Authorization", "Bearer " + manager))
                .andExpect(status().isOk());

        String roster = mvc.perform(get("/api/v1/rosters/" + rosterId)
                        .header("Authorization", "Bearer " + manager))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(roster).get("status").asText()).isEqualTo("PUBLISHED");
    }

    private String skillsOf(String empNo) {
        return switch (empNo) {
            case "EMP-201" -> "NURSE,CARE";
            case "EMP-202" -> "NURSE";
            case "EMP-203" -> "CARE,DRIVER";
            case "EMP-204" -> "NURSE,DRIVER";
            case "EMP-205" -> "CARE";
            default -> "";
        };
    }
}
