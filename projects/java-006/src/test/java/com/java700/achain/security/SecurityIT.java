package com.java700.achain.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.achain.common.TestDb;
import com.java700.achain.common.TestFixtures;
import java.time.Clock;
import java.time.Instant;
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
@SpringBootTest(classes = com.java700.achain.ApprovalChainApplication.class)
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

    private String requester;
    private String manager;
    private String auditor;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "requester", Roles.REQUESTER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "manager", Roles.MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        requester = TestFixtures.token(mvc, mapper, "requester");
        manager = TestFixtures.token(mvc, mapper, "manager");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    @Test
    void unauthenticatedAndForgedTokensRejected() throws Exception {
        mvc.perform(get("/api/v1/requests")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/requests").header("Authorization", "Bearer forged"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void auditorCannotCreateChainsOrPolicies() throws Exception {
        mvc.perform(post("/api/v1/chains")
                        .header("Authorization", "Bearer " + auditor)
                        .header("Idempotency-Key", "sec-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of(
                                "chainCode", "SEC-C", "name", "C",
                                "stepsJson", "[{\"step\":1,\"role\":\"MANAGER\",\"approversRequired\":1}]"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/policies")
                        .header("Authorization", "Bearer " + auditor)
                        .header("Idempotency-Key", "sec-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of(
                                "policyCode", "SEC-P", "name", "P", "rulesJson", "{}"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditorReadsRequestsAndDecisions() throws Exception {
        mvc.perform(get("/api/v1/requests").header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/chains").header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk());
    }

    @Test
    void lockoutAfterRepeatedFailures() throws Exception {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, "locktarget", encoder.encode(TestFixtures.PASSWORD),
                "locktarget@corp.example", null, Instant.now(clock)));
        localUsers.saveRole(id, Roles.AUDITOR);
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

    @Test
    void sqlInjectionAttemptsAreHarmless() throws Exception {
        mvc.perform(get("/api/v1/requests").header("Authorization", "Bearer " + auditor)
                        .param("status", "PENDING'; DROP TABLE approval_requests;--"))
                .andExpect(status().isOk());
    }
}
