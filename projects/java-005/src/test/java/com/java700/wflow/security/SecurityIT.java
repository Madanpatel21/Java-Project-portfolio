package com.java700.wflow.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.wflow.common.TestDb;
import com.java700.wflow.common.TestFixtures;
import java.time.Clock;
import java.time.Instant;
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

/** Authorization matrix + attack-surface tests. */
@SpringBootTest(classes = com.java700.wflow.WorkflowOrchestratorApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIT {

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

    private String padmin;
    private String operator;
    private String approver;
    private String viewer;

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
        viewer = TestFixtures.token(mvc, mapper, "viewer");
    }

    @Test
    void unauthenticatedAndForgedTokensRejected() throws Exception {
        mvc.perform(get("/api/v1/definitions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/definitions").header("Authorization", "Bearer forged"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorCannotCreateDefinitions() throws Exception {
        mvc.perform(post("/api/v1/definitions")
                        .header("Authorization", "Bearer " + operator)
                        .header("Idempotency-Key", "sec-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "definitionKey", "X", "name", "X",
                                "definitionJson", "{\"nodes\":[{\"id\":\"s\",\"type\":\"START\",\"next\":\"e\"},{\"id\":\"e\",\"type\":\"END\"}]}"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerIsReadOnly() throws Exception {
        mvc.perform(get("/api/v1/definitions").header("Authorization", "Bearer " + viewer))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/instances")
                        .header("Authorization", "Bearer " + viewer)
                        .header("Idempotency-Key", "sec-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("definitionKey", "X"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void malformedDefinitionJsonRejected() throws Exception {
        mvc.perform(post("/api/v1/definitions")
                        .header("Authorization", "Bearer " + padmin)
                        .header("Idempotency-Key", "sec-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "definitionKey", "BAD", "name", "Bad",
                                "definitionJson", "{\"nodes\":[{\"id\":\"e\",\"type\":\"END\"}]}"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sqlInjectionAttemptsAreHarmless() throws Exception {
        mvc.perform(get("/api/v1/definitions").header("Authorization", "Bearer " + padmin)
                        .param("key", "'; DROP TABLE workflow_instances;--"))
                .andExpect(status().isOk());
    }

    @Test
    void lockoutAfterRepeatedFailures() throws Exception {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, "locktarget", encoder.encode(TestFixtures.PASSWORD),
                "locktarget@corp.example", null, Instant.now(clock)));
        localUsers.saveRole(id, Roles.VIEWER);
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"locktarget\",\"password\":\"wrong\"}"))
                    .andExpect(status().isNotFound());
        }
        mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"locktarget\",\"password\":\"wrong\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"locktarget\",\"password\":\""
                                + TestFixtures.PASSWORD + "\"}"))
                .andExpect(status().isTooManyRequests());
    }
}
