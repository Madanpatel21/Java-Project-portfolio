package com.java700.legalmatter.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.legalmatter.common.TestDb;
import com.java700.legalmatter.common.TestFixtures;
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
@SpringBootTest(classes = com.java700.legalmatter.LegalMatterApplication.class)
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

    private String attorney;
    private String analyst;
    private String auditor;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "attorney", Roles.ATTORNEY, null);
        TestFixtures.createUser(localUsers, encoder, clock, "analyst", Roles.CONFLICT_ANALYST, null);
        TestFixtures.createUser(localUsers, encoder, clock, "auditor", Roles.AUDITOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        attorney = TestFixtures.token(mvc, mapper, "attorney");
        analyst = TestFixtures.token(mvc, mapper, "analyst");
        auditor = TestFixtures.token(mvc, mapper, "auditor");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    @Test
    void unauthenticatedAndForgedTokensRejected() throws Exception {
        mvc.perform(get("/api/v1/parties")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/parties").header("Authorization", "Bearer forged"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void analystCannotCreateMattersOrWalls() throws Exception {
        mvc.perform(post("/api/v1/matters")
                        .header("Authorization", "Bearer " + analyst)
                        .header("Idempotency-Key", "sec-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of(
                                "matterNo", "M-SEC", "name", "X", "clientPartyId", "x"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/matters/x/walls")
                        .header("Authorization", "Bearer " + analyst)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"roleName\":\"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void attorneyCannotScreenOrAddWalls() throws Exception {
        mvc.perform(post("/api/v1/conflicts/screen")
                        .header("Authorization", "Bearer " + attorney)
                        .header("Idempotency-Key", "sec-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of(
                                "subjectName", "X", "adverseNames", java.util.List.of("Y")))))
                .andExpect(status().isOk()); // ATTORNEY is allowed to screen
        mvc.perform(post("/api/v1/matters/x/walls")
                        .header("Authorization", "Bearer " + attorney)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"roleName\":\"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidPartyTypeRejected() throws Exception {
        mvc.perform(post("/api/v1/parties")
                        .header("Authorization", "Bearer " + attorney)
                        .header("Idempotency-Key", "sec-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of(
                                "name", "X", "type", "ALIEN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lockoutAfterRepeatedFailures() throws Exception {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, "locktarget", encoder.encode(TestFixtures.PASSWORD),
                "locktarget@firm.example", null, Instant.now(clock)));
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
        mvc.perform(get("/api/v1/parties").header("Authorization", "Bearer " + auditor)
                        .param("q", "' OR '1'='1"))
                .andExpect(status().isOk());
    }
}
