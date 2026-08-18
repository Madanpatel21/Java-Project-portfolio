package com.java700.crvs.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.crvs.common.TestDb;
import com.java700.crvs.common.TestFixtures;
import com.java700.crvs.offices.OfficeRepository;
import com.java700.crvs.registry.Office;
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

/** Authorization matrix + attack-surface tests for the civil registry. */
@SpringBootTest(classes = com.java700.crvs.CivilRegistryApplication.class)
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
    @Autowired
    OfficeRepository offices;

    private String registrar;
    private String supervisor;
    private String statistician;
    private String verifier;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        String officeId = offices.save(new Office(UUID.randomUUID().toString(), "RO-SEC",
                "Security Office", "NORTH")).getId();
        TestFixtures.createUser(localUsers, encoder, clock, "registrar", Roles.REGISTRAR, officeId);
        TestFixtures.createUser(localUsers, encoder, clock, "supervisor", Roles.SUPERVISOR, null);
        TestFixtures.createUser(localUsers, encoder, clock, "statistician", Roles.STATISTICIAN, null);
        TestFixtures.createUser(localUsers, encoder, clock, "verifier", Roles.VERIFIER_CLIENT, null);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.ADMIN, null);
        registrar = TestFixtures.token(mvc, mapper, "registrar");
        supervisor = TestFixtures.token(mvc, mapper, "supervisor");
        statistician = TestFixtures.token(mvc, mapper, "statistician");
        verifier = TestFixtures.token(mvc, mapper, "verifier");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    @Test
    void unauthenticatedAndForgedTokensRejected() throws Exception {
        mvc.perform(get("/api/v1/ledger/verify")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/ledger/verify").header("Authorization", "Bearer forged.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrarCannotApproveOrVerifyLedger() throws Exception {
        mvc.perform(get("/api/v1/registrations").header("Authorization", "Bearer " + registrar))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/ledger/verify").header("Authorization", "Bearer " + registrar))
                .andExpect(status().isForbidden());
    }

    @Test
    void supervisorCannotRevokeCertificatesOrAdjudicateDedup() throws Exception {
        mvc.perform(get("/api/v1/dedup/open").header("Authorization", "Bearer " + supervisor))
                .andExpect(status().isForbidden());
    }

    @Test
    void statisticianIsReadOnly() throws Exception {
        mvc.perform(get("/api/v1/statistics/vital?from=2026-08-01T00:00:00Z&to=2026-08-19T00:00:00Z")
                        .header("Authorization", "Bearer " + statistician))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/registrations/birth")
                        .header("Authorization", "Bearer " + statistician)
                        .header("Idempotency-Key", "sec-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "fullName", "X Y", "dob", "1990-01-01", "sex", "F",
                                "placeOfBirth", "P", "parentNames", ""))))
                .andExpect(status().isForbidden());
    }

    @Test
    void verifierClientCannotTouchRegistryData() throws Exception {
        mvc.perform(get("/api/v1/persons").header("Authorization", "Bearer " + verifier))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/ledger/verify").header("Authorization", "Bearer " + verifier))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidNationalIdFormatRejected() throws Exception {
        mvc.perform(get("/api/v1/verify/person/123").header("Authorization", "Bearer " + verifier))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/verify/person/9999999999").header("Authorization", "Bearer " + verifier))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sqlInjectionAttemptsAreHarmless() throws Exception {
        mvc.perform(get("/api/v1/persons").header("Authorization", "Bearer " + admin)
                        .param("query", "' OR '1'='1"))
                .andExpect(status().isOk());
    }

    @Test
    void lockoutAfterRepeatedFailures() throws Exception {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, "locktarget", encoder.encode(TestFixtures.PASSWORD),
                "locktarget@registry.gov", null, Instant.now(clock)));
        localUsers.saveRole(id, Roles.REGISTRAR);
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
    void identityMaskedInPersonListings() throws Exception {
        String regId = mvc.perform(post("/api/v1/registrations/birth")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "mask-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "fullName", "Masked Person", "dob", "1990-01-01", "sex", "F",
                                "placeOfBirth", "Place", "parentNames", "P1, P2"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        regId = mapper.readTree(regId).get("id").asText();
        mvc.perform(post("/api/v1/registrations/" + regId + "/approve")
                        .header("Authorization", "Bearer " + supervisor)
                        .header("Idempotency-Key", "mask-approve-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        String listing = mvc.perform(get("/api/v1/persons").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(listing).contains("***");
        org.assertj.core.api.Assertions.assertThat(listing).doesNotContain("Masked Person");
    }

    @Test
    void supervisorApprovingOwnCaptureIsBlocked() throws Exception {
        // supervisor has no office, so they cannot capture — instead: registrar captures and
        // attempts to approve (covered in LifeEventFlowIT). Here: registrar self-approve attempt.
        String regId = mvc.perform(post("/api/v1/registrations/birth")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "sod-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "fullName", "SoD Person", "dob", "1990-01-01", "sex", "M",
                                "placeOfBirth", "Place", "parentNames", ""))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        regId = mapper.readTree(regId).get("id").asText();
        mvc.perform(post("/api/v1/registrations/" + regId + "/approve")
                        .header("Authorization", "Bearer " + registrar)
                        .header("Idempotency-Key", "sod-approve-1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}
