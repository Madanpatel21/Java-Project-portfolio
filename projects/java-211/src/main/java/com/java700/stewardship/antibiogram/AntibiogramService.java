package com.java700.stewardship.antibiogram;

import com.java700.stewardship.microbiology.CultureRepository;
import com.java700.stewardship.microbiology.IsolateRepository;
import com.java700.stewardship.microbiology.SusceptibilityRepository;
import com.java700.stewardship.catalog.AntimicrobialDrug;
import com.java700.stewardship.catalog.DrugRepository;
import com.java700.stewardship.microbiology.Culture;
import com.java700.stewardship.microbiology.Isolate;
import com.java700.stewardship.microbiology.SusceptibilityResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Antibiogram aggregation with first-isolate deduplication: per patient and organism only
 * the first isolate within any rolling window (default 7 days) counts. Percentages are
 * computed over S+I+R; reporting is gated on a minimum isolate count (default 30).
 */
@Service
public class AntibiogramService {

    private final CultureRepository cultureRepository;
    private final IsolateRepository isolateRepository;
    private final SusceptibilityRepository susceptibilityRepository;
    private final DrugRepository drugRepository;
    private final int minIsolates;
    private final int dedupDays;

    public AntibiogramService(CultureRepository cultureRepository, IsolateRepository isolateRepository,
                              SusceptibilityRepository susceptibilityRepository,
                              DrugRepository drugRepository,
                              @Value("${app.stewardship.antibiogram-min-isolates:30}") int minIsolates,
                              @Value("${app.stewardship.isolate-dedup-days:7}") int dedupDays) {
        this.cultureRepository = cultureRepository;
        this.isolateRepository = isolateRepository;
        this.susceptibilityRepository = susceptibilityRepository;
        this.drugRepository = drugRepository;
        this.minIsolates = minIsolates;
        this.dedupDays = dedupDays;
    }

    @Transactional(readOnly = true)
    public AntibiogramReport report() {
        List<CountingIsolate> counting = new ArrayList<>();
        for (Culture culture : cultureRepository.findAll()) {
            if (culture.getReportedAt() == null) {
                continue;
            }
            String patientId = culture.getPatientId();
            for (Isolate isolate : isolateRepository.findByCultureId(culture.getId())) {
                counting.add(new CountingIsolate(patientId, isolate.getOrganism(),
                        isolate.getCollectedAt(), isolate.getId()));
            }
        }
        // first-isolate-per-patient-per-organism within the dedup window
        List<CountingIsolate> deduped = dedupe(counting);
        Map<String, Map<String, int[]>> counts = new LinkedHashMap<>(); // organism -> drugCode -> [S,I,R]
        Map<String, String> organismDrugNames = new LinkedHashMap<>();
        for (CountingIsolate iso : deduped) {
            for (SusceptibilityResult s : susceptibilityRepository.findByIsolateId(iso.isolateId)) {
                AntimicrobialDrug drug = drugRepository.findById(s.getDrugId()).orElse(null);
                if (drug == null) {
                    continue;
                }
                counts.computeIfAbsent(iso.organism, k -> new LinkedHashMap<>())
                        .computeIfAbsent(drug.getCode(), k -> new int[3]);
                int[] tally = counts.get(iso.organism).get(drug.getCode());
                switch (s.getResult()) {
                    case "S" -> tally[0]++;
                    case "I" -> tally[1]++;
                    case "R" -> tally[2]++;
                    default -> { }
                }
                organismDrugNames.putIfAbsent(iso.organism + "|" + drug.getCode(), drug.getName());
            }
        }
        List<AntibiogramRow> rows = new ArrayList<>();
        counts.forEach((organism, byDrug) -> byDrug.forEach((drugCode, tally) -> {
            int total = tally[0] + tally[1] + tally[2];
            rows.add(new AntibiogramRow(organism,
                    organismDrugNames.get(organism + "|" + drugCode), drugCode,
                    total, tally[0], tally[1], tally[2],
                    pct(tally[0], total), pct(tally[1], total), pct(tally[2], total),
                    total >= minIsolates));
        }));
        rows.sort(Comparator.comparing(AntibiogramRow::organism)
                .thenComparing(AntibiogramRow::drugCode));
        return new AntibiogramReport(minIsolates, dedupDays, rows);
    }

    private List<CountingIsolate> dedupe(List<CountingIsolate> all) {
        Map<String, List<CountingIsolate>> byPatientOrganism = new LinkedHashMap<>();
        for (CountingIsolate iso : all) {
            byPatientOrganism.computeIfAbsent(iso.patientId + "|" + iso.organism, k -> new ArrayList<>())
                    .add(iso);
        }
        List<CountingIsolate> kept = new ArrayList<>();
        byPatientOrganism.values().forEach(list -> {
            list.sort(Comparator.comparing(CountingIsolate::collectedAt));
            Instant lastKept = null;
            for (CountingIsolate iso : list) {
                if (lastKept == null
                        || Duration.between(lastKept, iso.collectedAt).toDays() > dedupDays) {
                    kept.add(iso);
                    lastKept = iso.collectedAt;
                }
            }
        });
        return kept;
    }

    private static BigDecimal pct(int part, int total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(part * 100L).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private record CountingIsolate(String patientId, String organism, Instant collectedAt,
                                   String isolateId) {
    }

    public record AntibiogramRow(String organism, String drugName, String drugCode, int isolates,
                                 int susceptible, int intermediate, int resistant,
                                 BigDecimal percentS, BigDecimal percentI, BigDecimal percentR,
                                 boolean reportable) {
    }

    public record AntibiogramReport(int minimumIsolates, int dedupWindowDays,
                                    List<AntibiogramRow> rows) {

        public AntibiogramReport {
            rows = List.copyOf(rows);
        }
    }
}
