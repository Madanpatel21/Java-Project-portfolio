package com.java700.crvs.statistics;


import com.java700.crvs.common.api.Problems;
import com.java700.crvs.ledger.LifeEvent;
import com.java700.crvs.ledger.LifeEventRepository;
import com.java700.crvs.registry.PersonRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vital statistics derived from the life-event ledger: births, deaths, marriages and
 * natural increase per region inside a UTC window. Region is resolved via the person record.
 */
@Service
public class VitalStatistics {

    private final LifeEventRepository ledger;
    private final PersonRepository persons;

    public VitalStatistics(LifeEventRepository ledger, PersonRepository persons) {
        this.ledger = ledger;
        this.persons = persons;
    }

    @Transactional(readOnly = true)
    public VitalReport report(Instant from, Instant to) {
        if (!to.isAfter(from)) {
            throw new Problems.BadRequest("to must be after from");
        }
        Map<String, long[]> byRegion = new LinkedHashMap<>();
        long[] totals = new long[4];
        for (LifeEvent e : ledger.findAll()) {
            if (e.getOccurredAt().isBefore(from) || !e.getOccurredAt().isBefore(to)) {
                continue;
            }
            String region = regionOf(e);
            if (region == null) {
                continue;
            }
            long[] r = byRegion.computeIfAbsent(region, k -> new long[4]);
            switch (e.getEventType()) {
                case "BIRTH" -> {
                    r[0]++;
                    totals[0]++;
                }
                case "DEATH" -> {
                    r[1]++;
                    totals[1]++;
                }
                case "MARRIAGE" -> {
                    r[2]++;
                    totals[2]++;
                }
                default -> { }
            }
        }
        java.util.List<RegionRow> rows = new java.util.ArrayList<>();
        byRegion.forEach((region, r) -> rows.add(new RegionRow(region, r[0], r[1], r[2],
                r[0] - r[1])));
        rows.sort(java.util.Comparator.comparing(RegionRow::region));
        return new VitalReport(from, to, totals[0], totals[1], totals[2],
                totals[0] - totals[1], rows);
    }

    private String regionOf(LifeEvent event) {
        return persons.findById(event.getPersonId())
                .map(p -> p.getRegion())
                .orElse("UNKNOWN");
    }

    public record RegionRow(String region, long births, long deaths, long marriages,
                            long naturalIncrease) {
    }

    public record VitalReport(Instant from, Instant to, long births, long deaths, long marriages,
                              long naturalIncrease, java.util.List<RegionRow> regions) {

        public VitalReport {
            regions = java.util.List.copyOf(regions);
        }
    }
}
