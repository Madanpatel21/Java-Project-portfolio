package com.java700.workforce.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.workforce.identity.UserProfile;
import com.java700.workforce.WorkforceComplianceApplication;
import com.java700.workforce.identity.UserProfileRepository;
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

/**
 * Authorization matrix + attack-surface tests: privilege escalation, broken access control,
 * token attacks, replay protection, injection attempts.
 */
@SpringBootTest(classes = WorkforceComplianceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    LocalUserService localUsers;
    @Autowired
    UserProfileRepository profiles;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    Clock clock;

    private String employee, manager, officer, admin, auditor, integrator;

    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbc;

    @BeforeEach
    void setUp() throws Exception {
        com.java700.workforce.common.TestDb.clean(jdbc);
        String hash = encoder.encode("Password123!");
        employee = create("emp", Roles.EMPLOYEE, hash);
        manager = create("mgr", Roles.ACCESS_MANAGER, hash);
        officer = create("off", Roles.COMPLIANCE_OFFICER, hash);
        admin = create("adm", Roles.COMPLIANCE_ADMIN, hash);
        auditor = create("aud", Roles.AUDITOR, hash);
        integrator = create("int", Roles.INTEGRATION, hash);
    }

    private String create(String username, String role, String hash) throws Exception {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, username, hash, username + "@corp.example", "UNIT",
                Instant.now(clock)));
        localUsers.saveRole(id, role);
        profiles.save(new UserProfile(id, username, username + "@corp.example", "UNIT", null,
                Instant.now(clock)));
        return token(username);
    }

    private String token(String username) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("accessToken").asText();
    }

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mvc.perform(get("/api/v1/evidence/verify")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/violations")).andExpect(status().isUnauthorized());
    }

    @Test
    void forgedTokenIsRejected() throws Exception {
        mvc.perform(get("/api/v1/violations")
                        .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9." + "forged." + "signature"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employeeCannotApproveEscalatingPrivileges() throws Exception {
        // employee hits a manager endpoint
        mvc.perform(post("/api/v1/recertification/campaigns")
                        .header("Authorization", "Bearer " + employee)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"windowDays\":7}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditorIsReadOnlyEverywhere() throws Exception {
        mvc.perform(get("/api/v1/evidence/verify").header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/violations").header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/policies").header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/compliance/run").header("Authorization", "Bearer " + auditor))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/recertification/campaigns")
                        .header("Authorization", "Bearer " + auditor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"windowDays\":7}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void integrationRoleCanIngestButCannotReadDirectory() throws Exception {
        mvc.perform(post("/api/v1/events/access")
                        .header("Authorization", "Bearer " + integrator)
                        .header("Idempotency-Key", "sec-it-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "userId", profiles.findAll().get(0).getId(),
                                "resourceName", "payroll-app", "eventType", "LOGIN",
                                "source", "hr-system"))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + integrator))
                .andExpect(status().isForbidden());
    }

    @Test
    void sqlInjectionAttemptsAreParameterizedAndHarmless() throws Exception {
        mvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + admin)
                        .param("query", "' OR '1'='1"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/violations").header("Authorization", "Bearer " + officer)
                        .param("status", "OPEN'; DROP TABLE violation;--"))
                .andExpect(status().isOk());
    }

    @Test
    void wrongCredentialsFailAndRepeatFailuresLockAccount() throws Exception {
        // dedicated user so the lockout never affects other tests' tokens
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, "locktarget", encoder.encode("Password123!"),
                "locktarget@corp.example", "UNIT", Instant.now(clock)));
        localUsers.saveRole(id, Roles.EMPLOYEE);
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"locktarget\",\"password\":\"wrong\"}"))
                    .andExpect(status().isNotFound());
        }
        // 5th failure locks
        mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"locktarget\",\"password\":\"wrong\"}"))
                .andExpect(status().isNotFound());
        // even the correct password is now rejected (locked)
        mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"locktarget\",\"password\":\"Password123!\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void idempotencyKeyReplayDoesNotDuplicateEvents() throws Exception {
        String key = "replay-" + UUID.randomUUID();
        String body = mapper.writeValueAsString(Map.of(
                "userId", profiles.findAll().get(0).getId(),
                "resourceName", "payroll-app", "eventType", "LOGIN", "source", "hr-system"));
        String first = mvc.perform(post("/api/v1/events/access")
                        .header("Authorization", "Bearer " + integrator)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String replay = mvc.perform(post("/api/v1/events/access")
                        .header("Authorization", "Bearer " + integrator)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(replay).contains("true"); // duplicate flag
        org.assertj.core.api.Assertions.assertThat(
                mapper.readTree(replay).get("eventId").asText())
                .isEqualTo(mapper.readTree(first).get("eventId").asText());
    }

    @Test
    void policyVersionCreationRequiresComplianceAdmin() throws Exception {
        String rules = "[{\"type\":\"SOD_CONFLICT\",\"severity\":\"HIGH\",\"params\":{\"conflictPairs\":[]}}]";
        mvc.perform(post("/api/v1/policies/ACCESS_GOVERNANCE/versions")
                        .header("Authorization", "Bearer " + officer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rulesJson\":" + mapper.writeValueAsString(rules) + "}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/policies/ACCESS_GOVERNANCE/versions")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rulesJson\":" + mapper.writeValueAsString(rules) + "}"))
                .andExpect(status().isOk());
    }
}
