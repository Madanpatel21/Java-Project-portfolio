package com.java700.stewardship.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.stewardship.common.TestDb;
import com.java700.stewardship.common.TestFixtures;
import com.java700.stewardship.patients.Patient;
import com.java700.stewardship.patients.PatientRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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

/**
 * Authorization matrix + attack-surface tests: escalation, read-only roles, PHI masking,
 * token forgery, injection attempts, lockout, idempotent replay.
 */
@SpringBootTest(classes = com.java700.stewardship.StewardshipApplication.class)
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
    PatientRepository patients;

    private String pharmacist;
    private String prescriber;
    private String idPhysician;
    private String microbiologist;
    private String infectionControl;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "pharmacist", Roles.PHARMACIST);
        TestFixtures.createUser(localUsers, encoder, clock, "prescriber", Roles.PRESCRIBER);
        TestFixtures.createUser(localUsers, encoder, clock, "idphysician", Roles.ID_PHYSICIAN);
        TestFixtures.createUser(localUsers, encoder, clock, "microbiologist", Roles.MICROBIOLOGIST);
        TestFixtures.createUser(localUsers, encoder, clock, "infectioncontrol", Roles.INFECTION_CONTROL);
        TestFixtures.createUser(localUsers, encoder, clock, "admin", Roles.STEWARDSHIP_ADMIN);
        pharmacist = TestFixtures.token(mvc, mapper, "pharmacist");
        prescriber = TestFixtures.token(mvc, mapper, "prescriber");
        idPhysician = TestFixtures.token(mvc, mapper, "idphysician");
        microbiologist = TestFixtures.token(mvc, mapper, "microbiologist");
        infectionControl = TestFixtures.token(mvc, mapper, "infectioncontrol");
        admin = TestFixtures.token(mvc, mapper, "admin");
    }

    private String createPatient() {
        return patients.save(new Patient(UUID.randomUUID().toString(), "MRN-SEC-" + UUID.randomUUID(),
                "Security Patient", LocalDate.of(1975, 5, 5), "F", new BigDecimal("58"))).getId();
    }

    @Test
    void unauthenticatedAndForgedTokensRejected() throws Exception {
        mvc.perform(get("/api/v1/drugs")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/drugs")
                        .header("Authorization", "Bearer forged.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pharmacistCannotApproveRestrictedDrugs() throws Exception {
        // approval is ID-physician-only
        String rxId = UUID.randomUUID().toString();
        mvc.perform(post("/api/v1/restricted-authorizations/" + rxId + "/approve")
                        .header("Authorization", "Bearer " + pharmacist))
                .andExpect(status().isForbidden());
    }

    @Test
    void prescriberCannotProposeInterventions() throws Exception {
        mvc.perform(post("/api/v1/interventions")
                        .header("Authorization", "Bearer " + prescriber)
                        .header("Idempotency-Key", "sec-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prescriptionId\":\"x\",\"type\":\"STOP\",\"reason\":\"r\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void infectionControlIsReadOnly() throws Exception {
        mvc.perform(get("/api/v1/antibiogram").header("Authorization", "Bearer " + infectionControl))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/guidelines")
                        .header("Authorization", "Bearer " + infectionControl)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rulesJson\":\"[]\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void phiMaskedInPatientViews() throws Exception {
        String patientId = createPatient();
        String body = mvc.perform(get("/api/v1/patients/" + patientId)
                        .header("Authorization", "Bearer " + pharmacist))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(body).doesNotContain("Security Patient");
        org.assertj.core.api.Assertions.assertThat(body).contains("***");
    }

    @Test
    void sqlInjectionAttemptsAreHarmless() throws Exception {
        mvc.perform(get("/api/v1/patients").header("Authorization", "Bearer " + pharmacist)
                        .param("query", "' OR '1'='1"))
                .andExpect(status().isOk());
    }

    @Test
    void policyVersionCreationRequiresAdmin() throws Exception {
        String rules = "[{\"type\":\"MAX_DURATION\",\"params\":{\"defaultDays\":5}}]";
        mvc.perform(post("/api/v1/guidelines")
                        .header("Authorization", "Bearer " + pharmacist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of("rulesJson", rules))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/guidelines")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of("rulesJson", rules))))
                .andExpect(status().isOk());
    }

    @Test
    void wrongCredentialsLockAccountAfterRepeatedFailures() throws Exception {
        String id = UUID.randomUUID().toString();
        localUsers.save(new LocalUser(id, "locktarget", encoder.encode(TestFixtures.PASSWORD),
                "locktarget@hospital.example", Instant.now(clock)));
        localUsers.saveRole(id, Roles.PHARMACIST);
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"locktarget\",\"password\":\"wrong\"}"))
                    .andExpect(status().isNotFound());
        }
        mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"locktarget\",\"password\":\"wrong\"}"))
                .andExpect(status().isNotFound());
        // locked: even the correct password is rejected
        mvc.perform(post("/api/v1/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"locktarget\",\"password\":\""
                                + TestFixtures.PASSWORD + "\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void invalidSusceptibilityResultRejected() throws Exception {
        String patientId = createPatient();
        String cultureId = mvc.perform(post("/api/v1/cultures")
                        .header("Authorization", "Bearer " + microbiologist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of(
                                "patientId", patientId, "specimenType", "BLOOD",
                                "collectedAt", Instant.now(clock).toString()))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(post("/api/v1/cultures/" + cultureId + "/isolates")
                        .header("Authorization", "Bearer " + microbiologist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Map.of(
                                "organism", "E. coli",
                                "susceptibility", java.util.List.of(
                                        java.util.Map.of("drugCode", "CEFTRIAXONE", "result", "X"))))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patientSearchRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/patients")).andExpect(status().isUnauthorized());
    }

    @Test
    void idempotentReplayReturnsOriginalIntervention() throws Exception {
        String patientId = createPatient();
        // create an active prescription directly
        String rxId = UUID.randomUUID().toString();
        jdbc.update("""
                insert into prescriptions
                (id, patient_id, admission_id, drug_id, indication, route, dose_mg, frequency_hours,
                 start_at, status, empiric, prescribed_by, created_at, version)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                """, rxId, patientId, "admission-x",
                "00000000-0000-0000-0000-000000000101", "SEPSIS", "IV", 1000, 8,
                java.sql.Timestamp.from(Instant.now(clock)), "ACTIVE", false, "dr",
                java.sql.Timestamp.from(Instant.now(clock)));
        String body = mapper.writeValueAsString(java.util.Map.of(
                "prescriptionId", rxId, "type", "STOP", "detail", java.util.Map.of(),
                "reason", "Therapy not indicated"));
        String first = mvc.perform(post("/api/v1/interventions")
                        .header("Authorization", "Bearer " + pharmacist)
                        .header("Idempotency-Key", "replay-sec-1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String replay = mvc.perform(post("/api/v1/interventions")
                        .header("Authorization", "Bearer " + pharmacist)
                        .header("Idempotency-Key", "replay-sec-1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(
                mapper.readTree(replay).get("id").asText())
                .isEqualTo(mapper.readTree(first).get("id").asText());
    }
}
