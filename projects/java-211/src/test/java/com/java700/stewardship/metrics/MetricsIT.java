package com.java700.stewardship.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import com.java700.stewardship.common.TestDb;
import com.java700.stewardship.patients.Admission;
import com.java700.stewardship.patients.AdmissionRepository;
import com.java700.stewardship.patients.Patient;
import com.java700.stewardship.patients.PatientRepository;
import com.java700.stewardship.prescriptions.Prescription;
import com.java700.stewardship.prescriptions.PrescriptionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** DOT / patient-days / DDD over seeded clinical data. */
@SpringBootTest(classes = com.java700.stewardship.StewardshipApplication.class)
@ActiveProfiles("test")
class MetricsIT {

    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    PatientRepository patients;
    @Autowired
    AdmissionRepository admissions;
    @Autowired
    PrescriptionRepository prescriptions;
    @Autowired
    UtilizationMetrics metrics;
    @Autowired
    Clock clock;

    private String patientId;
    private String admissionId;
    private Instant now;

    @BeforeEach
    void setUp() {
        TestDb.clean(jdbc);
        now = Instant.now(clock);
        Patient p = patients.save(new Patient(UUID.randomUUID().toString(), "MRN-MX-1",
                "Metrics Patient", LocalDate.of(1980, 1, 1), "M", new BigDecimal("75")));
        patientId = p.getId();
        Admission a = admissions.save(new Admission(UUID.randomUUID().toString(), patientId,
                "MED-MX", now.minus(6, ChronoUnit.DAYS), null));
        admissionId = a.getId();
    }

    private Prescription rx(String drugId, Instant start, Instant stop, String route) {
        Prescription r = new Prescription(UUID.randomUUID().toString(), patientId, admissionId,
                drugId, "SEPSIS", route, new BigDecimal("1000"), 8, start, false, "dr", null, start);
        r.activate();
        if (stop != null) {
            r.stop(stop);
        }
        return prescriptions.save(r);
    }

    @Test
    void dotAndPatientDaysComputedPerWard() {
        // ceftriaxone 3 days, meropenem 2 days (stopped)
        rx("00000000-0000-0000-0000-000000000101",
                now.minus(5, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS), "IV");
        rx("00000000-0000-0000-0000-000000000103",
                now.minus(4, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS), "IV");

        Instant from = now.minus(10, ChronoUnit.DAYS);
        Instant to = now.plus(1, ChronoUnit.DAYS);
        var report = metrics.compute(from, to, null);

        assertThat(report.wards()).hasSize(1);
        var ward = report.wards().get(0);
        assertThat(ward.ward()).isEqualTo("MED-MX");
        assertThat(ward.dot()).isEqualTo(5); // 3 + 2 days of therapy
        assertThat(ward.patientDays()).isEqualTo(6); // admitted 6 days ago, still in
        assertThat(ward.dotPer1000PatientDays()).isEqualByComparingTo("833.3");
    }

    @Test
    void wardFilterIsolatesScope() {
        rx("00000000-0000-0000-0000-000000000101",
                now.minus(2, ChronoUnit.DAYS), null, "IV");
        var report = metrics.compute(now.minus(10, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS), "ICU-1");
        assertThat(report.wards()).isEmpty();
        var full = metrics.compute(now.minus(10, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS), "MED-MX");
        assertThat(full.wards()).hasSize(1);
    }

    @Test
    void invalidWindowRejected() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                metrics.compute(now.plus(1, ChronoUnit.DAYS), now.minus(1, ChronoUnit.DAYS), null))
                .isInstanceOf(com.java700.stewardship.common.api.Problems.BadRequest.class);
    }

    @Test
    void dddComputedFromDoseFrequencyAndCatalog() {
        // ceftriaxone 1000mg Q8H = 3 g/day; DDD = 2 g → 1.5 DDD/day
        rx("00000000-0000-0000-0000-000000000101",
                now.minus(2, ChronoUnit.DAYS), null, "IV");
        var report = metrics.compute(now.minus(10, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS), null);
        assertThat(report.wards().get(0).ddd()).isEqualByComparingTo("3.0"); // 2 days × 1.5
    }
}
