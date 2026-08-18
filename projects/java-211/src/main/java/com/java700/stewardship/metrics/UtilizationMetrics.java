package com.java700.stewardship.metrics;

import com.java700.stewardship.catalog.AntimicrobialDrug;
import com.java700.stewardship.catalog.DrugRepository;
import com.java700.stewardship.interventions.Intervention;
import com.java700.stewardship.interventions.InterventionRepository;
import com.java700.stewardship.patients.Admission;
import com.java700.stewardship.patients.AdmissionRepository;
import com.java700.stewardship.prescriptions.Prescription;
import com.java700.stewardship.prescriptions.PrescriptionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hospital antimicrobial utilization: DOT (days of therapy), patient-days, DOT/1000,
 * DDD (defined daily doses) and intervention acceptance rates. All computations are
 * calendar-day based over a UTC window.
 */
@Service
public class UtilizationMetrics {

    private final PrescriptionRepository prescriptionRepository;
    private final AdmissionRepository admissionRepository;
    private final DrugRepository drugRepository;
    private final InterventionRepository interventionRepository;

    public UtilizationMetrics(PrescriptionRepository prescriptionRepository,
                              AdmissionRepository admissionRepository, DrugRepository drugRepository,
                              InterventionRepository interventionRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.admissionRepository = admissionRepository;
        this.drugRepository = drugRepository;
        this.interventionRepository = interventionRepository;
    }

    @Transactional(readOnly = true)
    public MetricsReport compute(Instant from, Instant to, String ward) {
        if (!to.isAfter(from)) {
            throw new com.java700.stewardship.common.api.Problems.BadRequest(
                    "to must be after from");
        }
        Map<String, Long> dotPerWard = new LinkedHashMap<>();
        Map<String, Long> patientDaysPerWard = new LinkedHashMap<>();
        Map<String, BigDecimal> dddPerWard = new LinkedHashMap<>();
        Set<String> ivToPo = new HashSet<>();

        for (Prescription rx : prescriptionRepository.findAll()) {
            if (rx.getStartAt() == null || rx.getStatus() == Prescription.Status.PENDING_AUTHORIZATION) {
                continue;
            }
            List<LocalDate> therapyDays = therapyDays(rx, from, to);
            if (therapyDays.isEmpty()) {
                continue;
            }
            String rxWard = wardOf(rx.getAdmissionId());
            if (rxWard == null || ward != null && !ward.equalsIgnoreCase(rxWard)) {
                continue;
            }
            dotPerWard.merge(rxWard, Long.valueOf(therapyDays.size()), Long::sum);
            AntimicrobialDrug drug = drugRepository.findById(rx.getDrugId()).orElse(null);
            if (drug != null) {
                BigDecimal dailyDoseGrams = rx.getDoseMg()
                        .multiply(BigDecimal.valueOf(24L / Math.max(1, rx.getFrequencyHours())))
                        .movePointLeft(3);
                BigDecimal ddd = dailyDoseGrams
                        .multiply(BigDecimal.valueOf(therapyDays.size()))
                        .divide(drug.getDddGrams(), 2, RoundingMode.HALF_UP);
                dddPerWard.merge(rxWard, ddd, BigDecimal::add);
            }
            if ("PO".equals(rx.getRoute())) {
                // IV->PO conversions: PO therapy on a drug with an IV form indicates a switch
                if (drug != null && drug.isIvAvailable()) {
                    ivToPo.add(rx.getId());
                }
            }
        }

        for (Admission admission : admissionRepository.findAll()) {
            long days = patientDays(admission, from, to);
            if (days > 0) {
                patientDaysPerWard.merge(admission.getWard(), days, Long::sum);
            }
        }

        Map<String, WardRow> wards = new LinkedHashMap<>();
        Set<String> allWards = new HashSet<>();
        allWards.addAll(dotPerWard.keySet());
        allWards.addAll(patientDaysPerWard.keySet());
        for (String w : allWards) {
            long dot = dotPerWard.getOrDefault(w, 0L);
            long pd = patientDaysPerWard.getOrDefault(w, 0L);
            BigDecimal per1000 = pd == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(dot * 1000L).divide(BigDecimal.valueOf(pd), 1, RoundingMode.HALF_UP);
            wards.put(w, new WardRow(w, dot, pd, per1000, dddPerWard.getOrDefault(w, BigDecimal.ZERO)));
        }

        List<Intervention> interventions = interventionRepository.findAll().stream()
                .filter(i -> !i.getProposedAt().isBefore(from) && !i.getProposedAt().isAfter(to))
                .toList();
        long decided = interventions.stream()
                .filter(i -> i.getStatus() != Intervention.Status.PROPOSED)
                .count();
        long accepted = interventions.stream()
                .filter(i -> i.getStatus() == Intervention.Status.ACCEPTED)
                .count();
        BigDecimal acceptance = decided == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(accepted * 100L).divide(BigDecimal.valueOf(decided), 1, RoundingMode.HALF_UP);

        return new MetricsReport(from, to, List.copyOf(wards.values()), ivToPo.size(),
                accepted, decided, acceptance);
    }

    /** Distinct calendar dates covered by [startAt, stopAt) within [from, to). */
    static List<LocalDate> therapyDays(Prescription rx, Instant from, Instant to) {
        LocalDate start = LocalDate.ofInstant(rx.getStartAt(), ZoneOffset.UTC);
        LocalDate windowStart = LocalDate.ofInstant(from, ZoneOffset.UTC);
        LocalDate windowEndExclusive = LocalDate.ofInstant(to, ZoneOffset.UTC);
        LocalDate endExclusive = rx.getStopAt() == null ? windowEndExclusive
                : LocalDate.ofInstant(rx.getStopAt(), ZoneOffset.UTC);
        LocalDate day = start.isBefore(windowStart) ? windowStart : start;
        LocalDate limit = endExclusive.isAfter(windowEndExclusive) ? windowEndExclusive : endExclusive;
        Set<LocalDate> days = new HashSet<>();
        while (day.isBefore(limit)) {
            days.add(day);
            day = day.plus(1, ChronoUnit.DAYS);
        }
        return days.stream().sorted().toList();
    }

    static long patientDays(Admission admission, Instant from, Instant to) {
        LocalDate admitted = LocalDate.ofInstant(admission.getAdmittedAt(), ZoneOffset.UTC);
        LocalDate windowStart = LocalDate.ofInstant(from, ZoneOffset.UTC);
        LocalDate windowEndExclusive = LocalDate.ofInstant(to, ZoneOffset.UTC);
        LocalDate discharged = admission.getDischargedAt() == null ? windowEndExclusive
                : LocalDate.ofInstant(admission.getDischargedAt(), ZoneOffset.UTC);
        LocalDate day = admitted.isBefore(windowStart) ? windowStart : admitted;
        LocalDate limit = discharged.isAfter(windowEndExclusive) ? windowEndExclusive : discharged;
        long count = 0;
        while (day.isBefore(limit)) {
            count++;
            day = day.plus(1, ChronoUnit.DAYS);
        }
        return count;
    }

    private final Map<String, String> wardCache = new HashMap<>();

    private String wardOf(String admissionId) {
        return wardCache.computeIfAbsent(admissionId, id ->
                admissionRepository.findById(id).map(Admission::getWard).orElse(null));
    }

    public record WardRow(String ward, long dot, long patientDays, BigDecimal dotPer1000PatientDays,
                          BigDecimal ddd) {
    }

    public record MetricsReport(Instant from, Instant to, List<WardRow> wards,
                                long ivToPoSwitches, long acceptedInterventions,
                                long decidedInterventions, BigDecimal acceptanceRatePercent) {

        public MetricsReport {
            wards = List.copyOf(wards);
        }
    }
}
