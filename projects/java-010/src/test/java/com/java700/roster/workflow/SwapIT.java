package com.java700.roster.workflow;

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

/** Shift swap (cover/exchange) workflow with manager approval and re-validation. */
@SpringBootTest(classes = com.java700.roster.RosterOptimizerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwapIT {

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
    private String employeeUserId;
    private String rosterId;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "manager", Roles.MANAGER, null);
        employeeUserId = TestFixtures.createUser(localUsers, encoder, clock, "employee",
                Roles.EMPLOYEE, null);
        manager = TestFixtures.token(mvc, mapper, "manager");
    }

    private void createEmployee(String empNo, String name, String skills, String userId)
            throws Exception {
        String body = String.format("{\"empNo\":\"%s\",\"name\":\"%s\",\"department\":\"OPS\","
                        + "\"skills\":\"%s\",\"employmentType\":\"FULL_TIME\","
                        + "\"maxWeeklyHours\":40}",
                empNo, name, skills);
        String response = mvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = mapper.readTree(response);
        if (userId != null) {
            jdbc.update("update employees set user_id = ? where id = ?", userId,
                    created.get("id").asText());
        }
    }

    private void setupRoster() throws Exception {
        LocalDate start = LocalDate.now(clock).plusDays(3);
        String body = String.format("{\"name\":\"Swap Week\",\"department\":\"OPS\","
                        + "\"startDate\":\"%s\",\"days\":3,"
                        + "\"demand\":[{\"shiftType\":\"MORNING\",\"startHour\":6,"
                        + "\"durationHours\":8,\"requiredSkill\":\"NURSE\",\"headcount\":1},"
                        + "{\"shiftType\":\"NIGHT\",\"startHour\":22,"
                        + "\"durationHours\":8,\"requiredSkill\":\"NURSE\",\"headcount\":1}]}",
                start);
        String response = mvc.perform(post("/api/v1/rosters")
                        .header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        rosterId = mapper.readTree(response).get("id").asText();
        mvc.perform(post("/api/v1/rosters/" + rosterId + "/optimize")
                        .header("Authorization", "Bearer " + manager))
                .andExpect(status().isOk());
    }

    private JsonNode findAssignmentOf(String empNo) throws Exception {
        String shiftsBody = mvc.perform(get("/api/v1/rosters/" + rosterId + "/shifts")
                        .header("Authorization", "Bearer " + manager))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode shift : mapper.readTree(shiftsBody)) {
            if (empNo.equals(shift.get("employeeEmpNo").asText())) {
                String assignments = jdbc.queryForObject(
                        "select id from shift_assignments where shift_id = ?",
                        String.class, shift.get("id").asText());
                return mapper.readTree("{\"assignmentId\":\"" + assignments
                        + "\",\"shiftDate\":\"" + shift.get("shiftDate").asText()
                        + "\",\"shiftType\":\"" + shift.get("shiftType").asText() + "\"}");
            }
        }
        throw new AssertionError("no assignment for " + empNo);
    }

    @Test
    void swapRequestIsApprovedAndAssignmentMoves() throws Exception {
        createEmployee("EMP-401", "Ana", "NURSE,CARE", employeeUserId);
        createEmployee("EMP-402", "Bob", "NURSE", null);
        createEmployee("EMP-403", "Carla", "CARE", null);
        setupRoster();

        JsonNode assignment = findAssignmentOf("EMP-401");
        String swapBody = mvc.perform(post("/api/v1/my/swaps")
                        .header("Authorization", "Bearer " + TestFixtures.token(mvc, mapper,
                                "employee"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignmentId\":\"" + assignment.get("assignmentId").asText()
                                + "\",\"targetEmployeeId\":\"" + employeeIdOf("EMP-402")
                                + "\",\"reason\":\"family event\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode swap = mapper.readTree(swapBody);
        assertThat(swap.get("status").asText()).isEqualTo("PENDING");

        // Manager approves: the assignment moves to EMP-402.
        mvc.perform(post("/api/v1/swaps/" + swap.get("id").asText() + "/decide")
                        .header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\",\"note\":\"ok\"}"))
                .andExpect(status().isOk());
        String moved = jdbc.queryForObject(
                "select employee_id from shift_assignments where id = ?",
                String.class, assignment.get("assignmentId").asText());
        assertThat(moved).isEqualTo(employeeIdOf("EMP-402"));
    }

    @Test
    void swapRejectedKeepsAssignmentAndSkillMismatchFailsFast() throws Exception {
        createEmployee("EMP-501", "Ana", "NURSE,CARE", employeeUserId);
        createEmployee("EMP-502", "Bob", "NURSE", null);
        createEmployee("EMP-503", "Carla", "CARE", null);
        setupRoster();

        // Skill mismatch: EMP-503 (CARE) cannot take a NURSE shift.
        JsonNode assignment = findAssignmentOf("EMP-501");
        if ("NURSE".equals(shiftSkillOf(assignment.get("assignmentId").asText()))) {
            mvc.perform(post("/api/v1/my/swaps")
                            .header("Authorization", "Bearer " + TestFixtures.token(mvc, mapper,
                                    "employee"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"assignmentId\":\""
                                    + assignment.get("assignmentId").asText()
                                    + "\",\"targetEmployeeId\":\"" + employeeIdOf("EMP-503")
                                    + "\",\"reason\":\"x\"}"))
                    .andExpect(status().isConflict());
        }

        String swapBody = mvc.perform(post("/api/v1/my/swaps")
                        .header("Authorization", "Bearer " + TestFixtures.token(mvc, mapper,
                                "employee"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignmentId\":\"" + assignment.get("assignmentId").asText()
                                + "\",\"targetEmployeeId\":\"" + employeeIdOf("EMP-502")
                                + "\",\"reason\":\"swap\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode swap = mapper.readTree(swapBody);

        mvc.perform(post("/api/v1/swaps/" + swap.get("id").asText() + "/decide")
                        .header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REJECTED\",\"note\":\"coverage risk\"}"))
                .andExpect(status().isOk());
        String kept = jdbc.queryForObject(
                "select employee_id from shift_assignments where id = ?",
                String.class, assignment.get("assignmentId").asText());
        assertThat(kept).isEqualTo(employeeIdOf("EMP-501"));

        // Employees cannot decide swaps.
        JsonNode second = mapper.readTree(mvc.perform(post("/api/v1/my/swaps")
                        .header("Authorization", "Bearer " + TestFixtures.token(mvc, mapper,
                                "employee"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignmentId\":\"" + assignment.get("assignmentId").asText()
                                + "\",\"targetEmployeeId\":\"" + employeeIdOf("EMP-502")
                                + "\",\"reason\":\"again\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        mvc.perform(post("/api/v1/swaps/" + second.get("id").asText() + "/decide")
                        .header("Authorization", "Bearer " + TestFixtures.token(mvc, mapper,
                                "employee"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVED\",\"note\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeCannotCreateRosters() throws Exception {
        String token = TestFixtures.token(mvc, mapper, "employee");
        mvc.perform(post("/api/v1/rosters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"department\":\"OPS\","
                                + "\"startDate\":\"2026-09-01\",\"days\":1,"
                                + "\"demand\":[{\"shiftType\":\"MORNING\",\"startHour\":6,"
                                + "\"durationHours\":8,\"requiredSkill\":\"NURSE\","
                                + "\"headcount\":1}]}"))
                .andExpect(status().isForbidden());
    }

    private String employeeIdOf(String empNo) {
        return jdbc.queryForObject("select id from employees where emp_no = ?",
                String.class, empNo);
    }

    private String shiftSkillOf(String assignmentId) {
        return jdbc.queryForObject(
                "select s.required_skill from shifts s join shift_assignments a "
                        + "on a.shift_id = s.id where a.id = ?",
                String.class, assignmentId);
    }
}
