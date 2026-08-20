package com.java700.wflow.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.wflow.common.TestDb;
import com.java700.wflow.common.TestFixtures;
import com.java700.wflow.domain.WorkflowTaskRepository;
import com.java700.wflow.security.LocalUserService;
import com.java700.wflow.security.Roles;
import java.time.Clock;
import java.time.Instant;
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

/** End-to-end: definition versioning, start, gateway routing, task completion, timer, cancel+compensation. */
@SpringBootTest(classes = com.java700.wflow.WorkflowOrchestratorApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowFlowIT {

    private static final String DEF = """
            {"nodes":[
              {"id":"start","type":"START","next":"review"},
              {"id":"review","type":"APPROVAL","role":"APPROVER","slaHours":24,"next":"gw1","compensation":"comp1"},
              {"id":"gw1","type":"GATEWAY","conditions":[
                 {"expr":"var.amount > 1000","next":"legal"},
                 {"default":true,"next":"end"}]},
              {"id":"legal","type":"APPROVAL","role":"LEGAL","slaHours":48,"next":"end"},
              {"id":"comp1","type":"COMPENSATION","role":"COMPENSATION","slaHours":24,"next":"end"},
              {"id":"end","type":"END"}
            ]}""";

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
    WorkflowTaskRepository tasks;

    private String padmin;
    private String operator;
    private String approver;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "padmin", Roles.PROCESS_ADMIN, null);
        TestFixtures.createUser(localUsers, encoder, clock, "operator", Roles.PROCESS_OPERATOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "approver", Roles.APPROVER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "viewer", Roles.VIEWER, null);
        padmin = TestFixtures.token(mvc, mapper, "padmin");
        operator = TestFixtures.token(mvc, mapper, "operator");
        approver = TestFixtures.token(mvc, mapper, "approver");
    }

    private String createDefinition() throws Exception {
        String body = mvc.perform(post("/api/v1/definitions")
                        .header("Authorization", "Bearer " + padmin)
                        .header("Idempotency-Key", "def-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "definitionKey", "EXPENSE_APPROVAL", "name", "Expense Approval",
                                "definitionJson", DEF))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asText();
    }

    @Test
    void highAmountRoutesThroughLegalReviewThenCompletes() throws Exception {
        createDefinition();
        // start with amount 5000
        String started = mvc.perform(post("/api/v1/instances")
                        .header("Authorization", "Bearer " + operator)
                        .header("Idempotency-Key", "inst-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "definitionKey", "EXPENSE_APPROVAL",
                                "businessKey", "EXP-9001",
                                "variables", Map.of("amount", 5000)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING_TASK"))
                .andReturn().getResponse().getContentAsString();
        String instanceId = mapper.readTree(started).get("id").asText();

        // first task: review (APPROVER)
        var pending = tasks.findByStatusAndAssigneeRole("PENDING", "APPROVER",
                org.springframework.data.domain.Pageable.unpaged());
        assertThat(pending.getTotalElements()).isEqualTo(1);
        String taskId = pending.getContent().get(0).getId();
        mvc.perform(post("/api/v1/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + approver)
                        .header("Idempotency-Key", "t1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":{\"approved\":true}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING_TASK"))
                .andExpect(jsonPath("$.currentNodeId").value("legal"));

        // legal task completes -> COMPLETED
        var legalTask = tasks.findByStatusAndAssigneeRole("PENDING", "LEGAL",
                org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);
        mvc.perform(post("/api/v1/tasks/" + legalTask.getId() + "/complete")
                        .header("Authorization", "Bearer " + approver)
                        .header("Idempotency-Key", "t2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":{\"approved\":true}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // idempotent replay of the task completion
        mvc.perform(post("/api/v1/tasks/" + legalTask.getId() + "/complete")
                        .header("Authorization", "Bearer " + approver)
                        .header("Idempotency-Key", "t2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":{\"approved\":true}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void lowAmountSkipsLegalAndCompletes() throws Exception {
        createDefinition();
        mvc.perform(post("/api/v1/instances")
                        .header("Authorization", "Bearer " + operator)
                        .header("Idempotency-Key", "inst-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "definitionKey", "EXPENSE_APPROVAL",
                                "businessKey", "EXP-9002",
                                "variables", Map.of("amount", 100)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING_TASK"));
        var pending = tasks.findByStatusAndAssigneeRole("PENDING", "APPROVER",
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        mvc.perform(post("/api/v1/tasks/" + pending.get(0).getId() + "/complete")
                        .header("Authorization", "Bearer " + approver)
                        .header("Idempotency-Key", "t3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":{\"approved\":true}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.currentNodeId").value("end"));
    }

    @Test
    void versioningDeprecatesPreviousAndNewInstancesUseNewVersion() throws Exception {
        String v1 = createDefinition();
        // create v2 with a different first task role
        String def2 = DEF.replace("\"role\":\"APPROVER\"", "\"role\":\"MANAGER\"");
        mvc.perform(post("/api/v1/definitions")
                        .header("Authorization", "Bearer " + padmin)
                        .header("Idempotency-Key", "def-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "definitionKey", "EXPENSE_APPROVAL", "name", "Expense Approval v2",
                                "definitionJson", def2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionNo").value(2));
        // v1 deprecated
        mvc.perform(get("/api/v1/definitions?key=EXPENSE_APPROVAL")
                        .header("Authorization", "Bearer " + padmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].versionNo").value(2))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].status").value("DEPRECATED"));
    }

    @Test
    void cancelCreatesCompensationTasks() throws Exception {
        createDefinition();
        String started = mvc.perform(post("/api/v1/instances")
                        .header("Authorization", "Bearer " + operator)
                        .header("Idempotency-Key", "inst-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "definitionKey", "EXPENSE_APPROVAL",
                                "businessKey", "EXP-9003",
                                "variables", Map.of("amount", 5000)))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String instanceId = mapper.readTree(started).get("id").asText();
        mvc.perform(post("/api/v1/instances/" + instanceId + "/cancel")
                        .header("Authorization", "Bearer " + operator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        long compensation = tasks.findAll().stream()
                .filter(t -> t.getType() == com.java700.wflow.domain.WorkflowTask.Type.COMPENSATION)
                .count();
        assertThat(compensation).isEqualTo(1);
        long pendingCompensation = tasks.findAll().stream()
                .filter(t -> t.getType() == com.java700.wflow.domain.WorkflowTask.Type.COMPENSATION
                        && t.getStatus() == com.java700.wflow.domain.WorkflowTask.Status.PENDING)
                .count();
        assertThat(pendingCompensation).isEqualTo(1);
    }
}
