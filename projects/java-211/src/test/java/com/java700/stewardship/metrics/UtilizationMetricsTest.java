package com.java700.stewardship.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import com.java700.stewardship.patients.Admission;
import com.java700.stewardship.prescriptions.Prescription;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class UtilizationMetricsTest {

    private static final Instant FROM = Instant.parse("2026-08-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-08-08T00:00:00Z");

    private static Prescription rx(Instant start, Instant stop) {
        Prescription r = new Prescription("rx1", "p1", "a1", "drug1", "SEPSIS", "IV",
                new BigDecimal("1000"), 8, start, false, "dr", null, start);
        if (stop != null) {
            r.stop(stop);
        }
        return r;
    }

    @Test
    void therapyDaysCountsDistinctCalendarDaysInsideWindow() {
        // 5 days of therapy fully inside the window
        List<LocalDate> days = UtilizationMetrics.therapyDays(
                rx(Instant.parse("2026-08-02T09:00:00Z"), Instant.parse("2026-08-07T09:00:00Z")),
                FROM, TO);
        assertThat(days).hasSize(5);
        assertThat(days).contains(LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 6));
    }

    @Test
    void therapyDaysClipsToWindowBoundaries() {
        // therapy started before the window and still running → clipped to window start
        List<LocalDate> days = UtilizationMetrics.therapyDays(
                rx(Instant.parse("2026-07-20T00:00:00Z"), null), FROM, TO);
        assertThat(days).hasSize(7);
        assertThat(days.get(0)).isEqualTo(LocalDate.of(2026, 8, 1));
    }

    @Test
    void stopDayIsExclusive() {
        // stops on 08-03 → therapy days are 08-01..08-02
        List<LocalDate> days = UtilizationMetrics.therapyDays(
                rx(Instant.parse("2026-08-01T06:00:00Z"), Instant.parse("2026-08-03T06:00:00Z")),
                FROM, TO);
        assertThat(days).hasSize(2);
        assertThat(days).doesNotContain(LocalDate.of(2026, 8, 3));
    }

    @Test
    void patientDaysCountsAdmissionOverlap() {
        Admission admission = new Admission("a1", "p1", "ICU-1",
                Instant.parse("2026-08-03T12:00:00Z"), Instant.parse("2026-08-06T12:00:00Z"));
        assertThat(UtilizationMetrics.patientDays(admission, FROM, TO)).isEqualTo(3);
    }

    @Test
    void patientDaysUnboundedWhileStillAdmitted() {
        Admission admission = new Admission("a1", "p1", "ICU-1",
                Instant.parse("2026-08-05T00:00:00Z"), null);
        assertThat(UtilizationMetrics.patientDays(admission, FROM, TO)).isEqualTo(3);
    }

    @Test
    void patientDaysZeroOutsideWindow() {
        Admission admission = new Admission("a1", "p1", "ICU-1",
                Instant.parse("2026-07-01T00:00:00Z"), Instant.parse("2026-07-20T00:00:00Z"));
        assertThat(UtilizationMetrics.patientDays(admission, FROM, TO)).isZero();
    }

    @Test
    void dotPer1000MathIsExact() {
        // 300 DOT / 200 patient-days → 1500.0 per 1000
        BigDecimal per1000 = BigDecimal.valueOf(300 * 1000L)
                .divide(BigDecimal.valueOf(200), 1, java.math.RoundingMode.HALF_UP);
        assertThat(per1000).isEqualByComparingTo("1500.0");
    }

    @Test
    void utcCalendarDaysUsedThroughout() {
        Instant start = Instant.parse("2026-08-02T23:30:00Z");
        LocalDate day = LocalDate.ofInstant(start, ZoneOffset.UTC);
        assertThat(day).isEqualTo(LocalDate.of(2026, 8, 2));
    }
}
