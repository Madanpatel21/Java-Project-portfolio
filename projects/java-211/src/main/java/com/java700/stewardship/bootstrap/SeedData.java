package com.java700.stewardship.bootstrap;

import com.java700.stewardship.microbiology.CultureRepository;
import com.java700.stewardship.catalog.DrugRepository;
import com.java700.stewardship.microbiology.Culture;
import com.java700.stewardship.patients.Admission;
import com.java700.stewardship.patients.LabValue;
import com.java700.stewardship.patients.Patient;
import com.java700.stewardship.patients.AdmissionRepository;
import com.java700.stewardship.patients.LabValueRepository;
import com.java700.stewardship.patients.PatientRepository;
import com.java700.stewardship.prescriptions.Prescription;
import com.java700.stewardship.prescriptions.PrescriptionRepository;
import com.java700.stewardship.security.LocalUser;
import com.java700.stewardship.security.LocalUserService;
import com.java700.stewardship.security.Roles;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Dev-profile demo dataset: users, patients, therapy with deliberate stewardship findings. */
@Configuration
@Profile("dev")
public class SeedData {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);
    private static final String PASSWORD = "Password123!";

    @Bean
    CommandLineRunner seed(LocalUserService localUsers, PatientRepository patients,
                           AdmissionRepository admissions, LabValueRepository labs,
                           PrescriptionRepository prescriptions, DrugRepository drugs,
                           CultureRepository cultures, PasswordEncoder encoder, Clock clock) {
        return args -> {
            if (patients.count() > 0) {
                return;
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);

            // users
            createUser(localUsers, "pharmacist", "pharmacist@hospital.example", Roles.PHARMACIST, hash, now);
            createUser(localUsers, "prescriber", "prescriber@hospital.example", Roles.PRESCRIBER, hash, now);
            createUser(localUsers, "idphysician", "idphysician@hospital.example", Roles.ID_PHYSICIAN, hash, now);
            createUser(localUsers, "microbiologist", "microbiologist@hospital.example", Roles.MICROBIOLOGIST, hash, now);
            createUser(localUsers, "infectioncontrol", "infectioncontrol@hospital.example", Roles.INFECTION_CONTROL, hash, now);
            createUser(localUsers, "admin", "admin@hospital.example", Roles.STEWARDSHIP_ADMIN, hash, now);

            // patients
            Patient p1 = patients.save(new Patient(UUID.randomUUID().toString(), "MRN-10001",
                    "Ada Lovelace", LocalDate.of(1942, 5, 12), "F", new BigDecimal("64.0")));
            Patient p2 = patients.save(new Patient(UUID.randomUUID().toString(), "MRN-10002",
                    "Alan Turing", LocalDate.of(1954, 6, 23), "M", new BigDecimal("78.0")));

            // admissions
            Admission a1 = admissions.save(new Admission(UUID.randomUUID().toString(), p1.getId(),
                    "ICU-1", now.minus(5, ChronoUnit.DAYS), null));
            Admission a2 = admissions.save(new Admission(UUID.randomUUID().toString(), p2.getId(),
                    "MED-2", now.minus(3, ChronoUnit.DAYS), null));

            // labs: p1 renal impairment (creatinine 2.1 mg/dL)
            labs.save(new LabValue(UUID.randomUUID().toString(), p1.getId(), "CREATININE",
                    new BigDecimal("2.1"), "mg/dL", now.minus(1, ChronoUnit.DAYS)));
            labs.save(new LabValue(UUID.randomUUID().toString(), p2.getId(), "CREATININE",
                    new BigDecimal("0.9"), "mg/dL", now.minus(1, ChronoUnit.DAYS)));

            // therapy for p1: empiric IV ceftriaxone running 3 days (review due at 48h)
            Prescription cef = new Prescription(UUID.randomUUID().toString(), p1.getId(), a1.getId(),
                    drugs.findByCode("CEFTRIAXONE").orElseThrow().getId(),
                    "COMMUNITY_PNEUMONIA", "IV", new BigDecimal("2000"), 24,
                    now.minus(3, ChronoUnit.DAYS), true, "system", null, now.minus(3, ChronoUnit.DAYS));
            cef.activate();
            prescriptions.save(cef);
            // p1: piperacillin-tazobactam (needs renal adjustment at CrCl < 40)
            Prescription p1Tazo = new Prescription(UUID.randomUUID().toString(), p1.getId(), a1.getId(),
                    drugs.findByCode("PIPERACILLIN_TAZOBACTAM").orElseThrow().getId(),
                    "SEPSIS", "IV", new BigDecimal("4500"), 8,
                    now.minus(2, ChronoUnit.DAYS), false, "system", null, now.minus(2, ChronoUnit.DAYS));
            p1Tazo.activate();
            prescriptions.save(p1Tazo);
            // p1: metronidazole IV → redundant anaerobic coverage with pip-tazo
            Prescription p1Metro = new Prescription(UUID.randomUUID().toString(), p1.getId(), a1.getId(),
                    drugs.findByCode("METRONIDAZOLE").orElseThrow().getId(),
                    "SEPSIS", "IV", new BigDecimal("500"), 8,
                    now.minus(1, ChronoUnit.DAYS), false, "system", null, now.minus(1, ChronoUnit.DAYS));
            p1Metro.activate();
            prescriptions.save(p1Metro);

            // p2: empiric IV amoxicillin-clavulanate for UTI (2 days, not yet review-due)
            Prescription amox = new Prescription(UUID.randomUUID().toString(), p2.getId(), a2.getId(),
                    drugs.findByCode("AMOXICILLIN_CLAVULANATE").orElseThrow().getId(),
                    "URINARY_TRACT", "IV", new BigDecimal("1200"), 8,
                    now.minus(2, ChronoUnit.DAYS), true, "system", null, now.minus(2, ChronoUnit.DAYS));
            amox.activate();
            prescriptions.save(amox);

            // culture for p1: E. coli — R to ceftriaxone (drug-bug mismatch), S to cefazolin
            Culture culture = cultures.save(new Culture(UUID.randomUUID().toString(), p1.getId(),
                    "BLOOD", now.minus(2, ChronoUnit.DAYS)));

            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      pharmacist / prescriber / idphysician
                      microbiologist / infectioncontrol / admin
                    Scenario:
                      Ada (ICU-1): empiric ceftriaxone 3d (review due) + pip-tazo
                        (renal dose) + metronidazole (redundant anaerobic coverage)
                      Alan (MED-2): empiric amox-clav for UTI
                      Culture {} collected for Ada — add isolates via the API
                    ==============================================================""",
                    PASSWORD, culture.getId());
        };
    }

    private static void createUser(LocalUserService service, String username, String email,
                                   String role, String hash, Instant now) {
        String id = UUID.randomUUID().toString();
        service.save(new LocalUser(id, username, hash, email, now));
        service.saveRole(id, role);
    }
}
