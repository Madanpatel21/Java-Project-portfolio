package com.java700.stewardship.microbiology;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.stewardship.common.TestDb;
import com.java700.stewardship.common.TestFixtures;
import com.java700.stewardship.patients.Admission;
import com.java700.stewardship.patients.AdmissionRepository;
import com.java700.stewardship.patients.Patient;
import com.java700.stewardship.patients.PatientRepository;
import com.java700.stewardship.prescriptions.Prescription;
import com.java700.stewardship.prescriptions.PrescriptionRepository;
import com.java700.stewardship.reviews.ReviewTask;
import com.java700.stewardship.reviews.ReviewTaskRepository;
import com.java700.stewardship.security.LocalUserService;
import com.java700.stewardship.security.Roles;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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

/** Culture reporting drives drug-bug mismatch alerts and review tasks. */
@SpringBootTest(classes = com.java700.stewardship.StewardshipApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CultureAlertIT {

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
    PrescriptionRepository prescriptions;
    @Autowired
    ReviewTaskRepository reviewTasks;

    private String microbiologist;
    private String pharmacist;
    private String patientId;
    private String admissionId;
    private String rxId;

    @BeforeEach
    void setUp() throws Exception {
        TestDb.clean(jdbc);
        TestFixtures.createUser(localUsers, encoder, clock, "microbiologist", Roles.MICROBIOLOGIST);
        TestFixtures.createUser(localUsers, encoder, clock, "pharmacist", Roles.PHARMACIST);
        microbiologist = TestFixtures.token(mvc, mapper, "microbiologist");
        pharmacist = TestFixtures.token(mvc, mapper, "pharmacist");
        Patient p = patients.save(new Patient(UUID.randomUUID().toString(), "MRN-CX-1",
                "Culture Patient", LocalDate.of(1955, 6, 1), "F", new BigDecimal("60")));
        patientId = p.getId();
        Admission a = admissions.save(new Admission(UUID.randomUUID().toString(), patientId,
                "MED-CX", Instant.now(clock), null));
        admissionId = a.getId();

        // active IV ceftriaxone therapy
        Prescription rx = new Prescription(UUID.randomUUID().toString(), patientId, admissionId,
                "00000000-0000-0000-0000-000000000101", "SEPSIS", "IV",
                new BigDecimal("2000"), 24, Instant.now(clock), true, "dr", null, Instant.now(clock));
        rx.activate();
        prescriptions.save(rx);
        rxId = rx.getId();
    }

    @Test
    void resistantIsolateCreatesCriticalReviewTask() throws Exception {
        // culture with E. coli resistant to ceftriaxone
        String cultureId = mvc.perform(post("/api/v1/cultures")
                        .header("Authorization", "Bearer " + microbiologist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "patientId", patientId, "specimenType", "BLOOD",
                                "collectedAt", Instant.now(clock).toString()))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mvc.perform(post("/api/v1/cultures/" + cultureId + "/isolates")
                        .header("Authorization", "Bearer " + microbiologist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "organism", "Escherichia coli",
                                "susceptibility", List.of(
                                        Map.of("drugCode", "CEFTRIAXONE", "result", "R"))))))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/cultures/" + cultureId + "/report")
                        .header("Authorization", "Bearer " + microbiologist))
                .andExpect(status().isOk());

        // direct bus → CRITICAL review task created synchronously
        List<ReviewTask> tasks = reviewTasks.findByStatusOrderByDueAtAsc("OPEN");
        assertThat(tasks).anyMatch(t -> t.getPrescriptionId().equals(rxId)
                && t.getTriggerReason().equals("DRUG_BUG_MISMATCH"));
    }

    @Test
    void evaluationEndpointShowsMismatchFinding() throws Exception {
        String cultureId = mvc.perform(post("/api/v1/cultures")
                        .header("Authorization", "Bearer " + microbiologist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "patientId", patientId, "specimenType", "BLOOD",
                                "collectedAt", Instant.now(clock).toString()))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(post("/api/v1/cultures/" + cultureId + "/isolates")
                        .header("Authorization", "Bearer " + microbiologist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "organism", "Klebsiella pneumoniae",
                                "susceptibility", List.of(
                                        Map.of("drugCode", "CEFTRIAXONE", "result", "R"),
                                        Map.of("drugCode", "CEFAZOLIN", "result", "S"))))))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/cultures/" + cultureId + "/report")
                        .header("Authorization", "Bearer " + microbiologist))
                .andExpect(status().isOk());

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/stewardship/evaluate/" + rxId)
                        .header("Authorization", "Bearer " + pharmacist))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.type=='DRUG_BUG_MISMATCH')]").exists());
    }
}
