package com.java700.contracts.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.contracts.common.TestDb;
import com.java700.contracts.common.TestFixtures;
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
@SpringBootTest(classes = com.java700.contracts.ContractLifecycleApplication.class)
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

    private String legal;
    private String cmanager;
    private String owner;
    private String finance;
    private String auditor;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "legal", Roles.LEGAL_COUNSEL, null);
        TestFixtures.createUser(localUsers, encoder, clock, "cmanager", Roles.CONTRACT_MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "owner", Roles.BUSINESS_OWNER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "finance", Roles.FINANCE, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        legal = TestFixtures.token(mvc, mapper, "legal");
        cmanager = TestFixtures.token(mvc, mapper, "cmanager");
        owner = TestFixtures.token(mvc, mapper, "owner");
        finance = TestFixtures.token(mvc, mapper, "finance");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    @Test
    void unauthenticatedAndForgedTokensRejected() throws Exception {
        mvc.perform(get("/api/v1/contracts")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/contracts").header("Authorization", "Bearer forged.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void businessOwnerCannotCreateContracts() throws Exception {
        mvc.perform(post("/api/v1/contracts")
                        .header("Authorization", "Bearer " + owner)
                        .header("Idempotency-Key", "sec-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "contractNo", "CT-SEC", "title", "T", "counterparty", "C"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void financeCannotWaiveObligations() throws Exception {
        mvc.perform(post("/api/v1/obligations/any/waive")
                        .header("Authorization", "Bearer " + finance)
                        .header("Idempotency-Key", "sec-2")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditorCannotMutateAnything() throws Exception {
        mvc.perform(post("/api/v1/contracts")
                        .header("Authorization", "Bearer " + auditor)
                        .header("Idempotency-Key", "sec-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "contractNo", "CT-SEC2", "title", "T", "counterparty", "C"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/contracts").header("Authorization", "Bearer " + auditor))
                .andExpect(status().isOk());
    }

    @Test
    void sqlInjectionAttemptsAreHarmless() throws Exception {
        mvc.perform(get("/api/v1/contracts").header("Authorization", "Bearer " + cmanager)
                        .param("status", "DRAFT'; DROP TABLE contracts;--"))
                .andExpect(status().isOk());
    }

    @Test
    void lockoutAfterRepeatedFailures() throws Exception {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, "locktarget", encoder.encode(TestFixtures.PASSWORD),
                "locktarget@corp.example", null, Instant.now(clock)));
        localUsers.saveRole(id, Roles.FINANCE);
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
    void invalidObligationTypeRejected() throws Exception {
        mvc.perform(post("/api/v1/obligations")
                        .header("Authorization", "Bearer " + cmanager)
                        .header("Idempotency-Key", "sec-4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "contractId", "x", "type", "VIBES",
                                "title", "T", "dueAt", Instant.now(clock).toString(),
                                "windowBeforeDays", 30, "criticality", "HIGH"))))
                .andExpect(status().isBadRequest());
    }
}
