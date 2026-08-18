package com.java700.stewardship.prescriptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.stewardship.common.TestDb;
import com.java700.stewardship.common.TestFixtures;
import com.java700.stewardship.interventions.Intervention;
import com.java700.stewardship.interventions.InterventionRepository;
import com.java700.stewardship.patients.Admission;
import com.java700.stewardship.patients.AdmissionRepository;
import com.java700.stewardship.patients.LabValue;
import com.java700.stewardship.patients.LabValueRepository;
import com.java700.stewardship.patients.Patient;
import com.java700.stewardship.patients.PatientRepository;
import com.java700.stewardship.restricted.RestrictedAuthRepository;
import com.java700.stewardship.reviews.ReviewTaskRepository;
import com.java700.stewardship.security.LocalUserService;
import com.java700.stewardship.security.Roles;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

/** End-to-end: restricted-drug pre-authorization, intervention lifecycle, idempotency. */
@SpringBootTest(classes = com.java700.stewardship.StewardshipApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrescriptionFlowIT {

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
    @Autowired
    AdmissionRepository admissions;
    @Autowired
    LabValueRepository labs;
    @Autowired
    PrescriptionRepository prescriptions;
    @Autowired
    RestrictedAuthRepository authorizations;
    @Autowired
    InterventionRepository interventions;
    @Autowired
    ReviewTaskRepository reviewTasks;

    private String pharmacist;
    private String prescriber;
    private String idPhysician;
    private String patientId;
    private String admissionId;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "pharmacist", Roles.PHARMACIST);
        TestFixtures.createUser(localUsers, encoder, clock, "prescriber", Roles.PRESCRIBER);
        TestFixtures.createUser(localUsers, encoder, clock, "idphysician", Roles.ID_PHYSICIAN);
        pharmacist = TestFixtures.token(mvc, mapper, "pharmacist");
        prescriber = TestFixtures.token(mvc, mapper, "prescriber");
        idPhysician = TestFixtures.token(mvc, mapper, "idphysician");
        Patient p = patients.save(new Patient(UUID.randomUUID().toString(), "MRN-IT-1",
                "Integration Patient", LocalDate.of(1970, 3, 1), "M", new BigDecimal("72")));
        patientId = p.getId();
        Admission a = admissions.save(new Admission(UUID.randomUUID().toString(), patientId,
                "ICU-IT", Instant.now(clock), null));
        admissionId = a.getId();
        labs.save(new LabValue(UUID.randomUUID().toString(), patientId, "CREATININE",
                new BigDecimal("1.1"), "mg/dL", Instant.now(clock)));
    }

    @Test
    void restrictedDrugRequiresPreAuthorizationThenActivates() throws Exception {
        // 1. prescriber orders meropenem (restricted)
        String created = mvc.perform(post("/api/v1/prescriptions")
                        .header("Authorization", "Bearer " + prescriber)
                        .header("Idempotency-Key", "rx-mero-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "patientId", patientId, "admissionId", admissionId,
                                "drugCode", "MEROPENEM", "indication", "SEPSIS",
                                "route", "IV", "doseMg", 1000, "frequencyHours", 8,
                                "startAt", Instant.now(clock).toString(), "empiric", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_AUTHORIZATION"))
                .andExpect(jsonPath("$.preAuthorizationRequired").value(true))
                .andReturn().getResponse().getContentAsString();
        String rxId = mapper.readTree(created).get("id").asText();

        // 2. idempotent replay returns the same prescription
        String replay = mvc.perform(post("/api/v1/prescriptions")
                        .header("Authorization", "Bearer " + prescriber)
                        .header("Idempotency-Key", "rx-mero-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "patientId", patientId, "admissionId", admissionId,
                                "drugCode", "MEROPENEM", "indication", "SEPSIS",
                                "route", "IV", "doseMg", 1000, "frequencyHours", 8,
                                "startAt", Instant.now(clock).toString(), "empiric", false))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(replay).get("id").asText()).isEqualTo(rxId);
        assertThat(prescriptions.count()).isEqualTo(1);

        // 3. ID physician approves → prescription ACTIVE
        String authId = authorizations.findAll().get(0).getId();
        mvc.perform(post("/api/v1/restricted-authorizations/" + authId + "/approve")
                        .header("Authorization", "Bearer " + idPhysician))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/patients/" + patientId + "/prescriptions")
                        .header("Authorization", "Bearer " + pharmacist))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void pharmacistInterventionAcceptedByPrescriberChangesTherapy() throws Exception {
        // non-restricted empiric ceftriaxone
        String created = mvc.perform(post("/api/v1/prescriptions")
                        .header("Authorization", "Bearer " + prescriber)
                        .header("Idempotency-Key", "rx-cef-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "patientId", patientId, "admissionId", admissionId,
                                "drugCode", "CEFTRIAXONE", "indication", "COMMUNITY_PNEUMONIA",
                                "route", "IV", "doseMg", 2000, "frequencyHours", 24,
                                "startAt", Instant.now(clock).toString(), "empiric", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        String rxId = mapper.readTree(created).get("id").asText();

        // pharmacist proposes IV→PO
        String proposed = mvc.perform(post("/api/v1/interventions")
                        .header("Authorization", "Bearer " + pharmacist)
                        .header("Idempotency-Key", "iv-po-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "prescriptionId", rxId, "type", "IV_TO_PO",
                                "detail", Map.of("doseMg", 1000, "frequencyHours", 24),
                                "reason", "Afebrile 72h, oral switch appropriate"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROPOSED"))
                .andReturn().getResponse().getContentAsString();
        String interventionId = mapper.readTree(proposed).get("id").asText();

        // prescriber accepts → therapy modified to PO
        mvc.perform(post("/api/v1/interventions/" + interventionId + "/accept")
                        .header("Authorization", "Bearer " + prescriber)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mvc.perform(get("/api/v1/patients/" + patientId + "/prescriptions")
                        .header("Authorization", "Bearer " + pharmacist))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].route").value("PO"));

        Intervention stored = interventions.findById(interventionId).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(Intervention.Status.ACCEPTED);
    }

    @Test
    void rejectionWithoutReasonIsBlocked() throws Exception {
        String created = mvc.perform(post("/api/v1/prescriptions")
                        .header("Authorization", "Bearer " + prescriber)
                        .header("Idempotency-Key", "rx-cef-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "patientId", patientId, "admissionId", admissionId,
                                "drugCode", "CEFTRIAXONE", "indication", "SEPSIS",
                                "route", "IV", "doseMg", 2000, "frequencyHours", 24,
                                "startAt", Instant.now(clock).toString(), "empiric", false))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String rxId = mapper.readTree(created).get("id").asText();

        String proposed = mvc.perform(post("/api/v1/interventions")
                        .header("Authorization", "Bearer " + pharmacist)
                        .header("Idempotency-Key", "stop-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"prescriptionId\":\"" + rxId + "\","
                                + "\"type\":\"STOP\","
                                + "\"detail\":{},"
                                + "\"reason\":\"Therapy not indicated\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String interventionId = mapper.readTree(proposed).get("id").asText();

        // rejection without reason → 400
        mvc.perform(post("/api/v1/interventions/" + interventionId + "/reject")
                        .header("Authorization", "Bearer " + prescriber)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
