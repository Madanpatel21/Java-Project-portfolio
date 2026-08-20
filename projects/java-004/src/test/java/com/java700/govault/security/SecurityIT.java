package com.java700.govault.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.govault.common.TestDb;
import com.java700.govault.common.TestFixtures;
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
@SpringBootTest(classes = com.java700.govault.DocumentGovernanceApplication.class,
        properties = "app.govault.content-dir=target/test-content")
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

    private String records;
    private String legal;
    private String auditor;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "records", Roles.RECORDS_MANAGER, null);
        TestFixtures.createUser(localUsers, encoder, clock, "legal", Roles.LEGAL_COUNSEL, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        records = TestFixtures.token(mvc, mapper, "records");
        legal = TestFixtures.token(mvc, mapper, "legal");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    @Test
    void unauthenticatedAndForgedTokensRejected() throws Exception {
        mvc.perform(get("/api/v1/documents")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/documents").header("Authorization", "Bearer forged"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void auditorCannotCreateHoldsOrClassify() throws Exception {
        mvc.perform(post("/api/v1/holds")
                        .header("Authorization", "Bearer " + auditor)
                        .header("Idempotency-Key", "sec-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"H\",\"reason\":\"R\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/documents/x/classify")
                        .header("Authorization", "Bearer " + auditor)
                        .header("Idempotency-Key", "sec-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"classification\":\"INTERNAL\",\"retentionClass\":\"R1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidClassificationRejected() throws Exception {
        mvc.perform(post("/api/v1/documents/x/classify")
                        .header("Authorization", "Bearer " + records)
                        .header("Idempotency-Key", "sec-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"classification\":\"TOP_SECRET\",\"retentionClass\":\"R1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidRetentionClassRejected() throws Exception {
        mvc.perform(post("/api/v1/documents/x/classify")
                        .header("Authorization", "Bearer " + records)
                        .header("Idempotency-Key", "sec-4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"classification\":\"INTERNAL\",\"retentionClass\":\"R99\"}"))
                .andExpect(status().isBadRequest());
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
        mvc.perform(get("/api/v1/documents/search").header("Authorization", "Bearer " + records)
                        .param("q", "' OR '1'='1"))
                .andExpect(status().isOk());
    }
}
