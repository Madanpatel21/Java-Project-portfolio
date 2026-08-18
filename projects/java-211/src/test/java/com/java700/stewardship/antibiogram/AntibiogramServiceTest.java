package com.java700.stewardship.antibiogram;

import com.java700.stewardship.microbiology.CultureRepository;
import com.java700.stewardship.microbiology.IsolateRepository;
import com.java700.stewardship.microbiology.SusceptibilityRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.java700.stewardship.catalog.AntimicrobialDrug;
import com.java700.stewardship.catalog.DrugRepository;
import com.java700.stewardship.microbiology.Culture;
import com.java700.stewardship.microbiology.Isolate;
import com.java700.stewardship.microbiology.SusceptibilityResult;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AntibiogramServiceTest {

    private CultureRepository cultureRepository;
    private IsolateRepository isolateRepository;
    private SusceptibilityRepository susceptibilityRepository;
    private DrugRepository drugRepository;
    private AntibiogramService service;

    @BeforeEach
    void setUp() {
        cultureRepository = mock(CultureRepository.class);
        isolateRepository = mock(IsolateRepository.class);
        susceptibilityRepository = mock(SusceptibilityRepository.class);
        drugRepository = mock(DrugRepository.class);
        service = new AntibiogramService(cultureRepository, isolateRepository,
                susceptibilityRepository, drugRepository, 5, 7);
    }

    private static final class DrugStub extends AntimicrobialDrug {
        private final String id;
        private final String code;
        private final String name;

        DrugStub(String id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private AntimicrobialDrug drug(String id, String code, String name) {
        return new DrugStub(id, code, name);
    }

    private Isolate isolate(String id, String cultureId, String organism, Instant collectedAt) {
        return new Isolate(id, cultureId, organism, collectedAt);
    }

    private Culture culture(String id, String patientId, Instant collectedAt) {
        Culture c = new Culture(id, patientId, "BLOOD", collectedAt);
        c.report(Instant.now());
        return c;
    }

    @Test
    void aggregatesPercentagesAndGatesReporting() {
        Instant t0 = Instant.parse("2026-08-01T00:00:00Z");
        // 6 isolates across 6 patients: E. coli vs CIPROFLOXACIN
        List<Culture> cultures = new java.util.ArrayList<>();
        List<Isolate> isolates = new java.util.ArrayList<>();
        List<SusceptibilityResult> results = new java.util.ArrayList<>();
        String[] res = {"S", "S", "S", "S", "R", "R"};
        for (int i = 0; i < 6; i++) {
            String cultureId = "c" + i;
            String isolateId = "iso" + i;
            cultures.add(culture(cultureId, "p" + i, t0));
            isolates.add(isolate(isolateId, cultureId, "E. coli", t0));
            results.add(new SusceptibilityResult("s" + i, isolateId, "CIPROFLOXACIN", res[i], null));
            when(isolateRepository.findByCultureId(cultureId))
                    .thenReturn(List.of(isolate(isolateId, cultureId, "E. coli", t0)));
            when(susceptibilityRepository.findByIsolateId(isolateId))
                    .thenReturn(List.of(new SusceptibilityResult("s" + i, isolateId,
                            "CIPROFLOXACIN", res[i], null)));
        }
        when(cultureRepository.findAll()).thenReturn(cultures);
        when(drugRepository.findById("CIPROFLOXACIN"))
                .thenReturn(Optional.of(drug("CIPROFLOXACIN", "CIPROFLOXACIN", "Ciprofloxacin")));

        AntibiogramService.AntibiogramReport report = service.report();
        assertThat(report.rows()).hasSize(1);
        var row = report.rows().get(0);
        assertThat(row.isolates()).isEqualTo(6);
        assertThat(row.susceptible()).isEqualTo(4);
        assertThat(row.resistant()).isEqualTo(2);
        assertThat(row.percentS()).isEqualByComparingTo("66.7");
        assertThat(row.percentR()).isEqualByComparingTo("33.3");
        assertThat(row.reportable()).isTrue(); // 6 >= min 5
    }

    @Test
    void belowMinimumIsolatesNotReportable() {
        Instant t0 = Instant.parse("2026-08-01T00:00:00Z");
        List<Culture> cultures = List.of(culture("c0", "p0", t0), culture("c1", "p1", t0));
        when(cultureRepository.findAll()).thenReturn(cultures);
        when(isolateRepository.findByCultureId("c0"))
                .thenReturn(List.of(isolate("iso0", "c0", "E. coli", t0)));
        when(isolateRepository.findByCultureId("c1"))
                .thenReturn(List.of(isolate("iso1", "c1", "E. coli", t0)));
        when(susceptibilityRepository.findByIsolateId("iso0"))
                .thenReturn(List.of(new SusceptibilityResult("s0", "iso0", "CIPROFLOXACIN", "S", null)));
        when(susceptibilityRepository.findByIsolateId("iso1"))
                .thenReturn(List.of(new SusceptibilityResult("s1", "iso1", "CIPROFLOXACIN", "S", null)));
        when(drugRepository.findById("CIPROFLOXACIN"))
                .thenReturn(Optional.of(drug("CIPROFLOXACIN", "CIPROFLOXACIN", "Ciprofloxacin")));

        var report = service.report();
        assertThat(report.rows()).hasSize(1);
        assertThat(report.rows().get(0).reportable()).isFalse(); // 2 < min 5
    }

    @Test
    void duplicateIsolatesWithinDedupWindowCountOnce() {
        Instant t0 = Instant.parse("2026-08-01T00:00:00Z");
        // same patient, same organism, two isolates 2 days apart → only first counts
        List<Culture> cultures = new java.util.ArrayList<>();
        List<Isolate> isolates = new java.util.ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String cultureId = "dup" + i;
            Instant at = t0.plus(i * 2, ChronoUnit.DAYS);
            cultures.add(culture(cultureId, "p0", at));
            isolates.add(isolate("diso" + i, cultureId, "E. coli", at));
            when(isolateRepository.findByCultureId(cultureId))
                    .thenReturn(List.of(isolate("diso" + i, cultureId, "E. coli", at)));
            when(susceptibilityRepository.findByIsolateId("diso" + i))
                    .thenReturn(List.of(new SusceptibilityResult("ds" + i, "diso" + i,
                            "CIPROFLOXACIN", "S", null)));
        }
        when(cultureRepository.findAll()).thenReturn(cultures);
        when(drugRepository.findById("CIPROFLOXACIN"))
                .thenReturn(Optional.of(drug("CIPROFLOXACIN", "CIPROFLOXACIN", "Ciprofloxacin")));

        var report = service.report();
        assertThat(report.rows().get(0).isolates()).isEqualTo(1);
    }

    @Test
    void isolatesBeyondDedupWindowCountAgain() {
        Instant t0 = Instant.parse("2026-08-01T00:00:00Z");
        List<Culture> cultures = new java.util.ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String cultureId = "win" + i;
            Instant at = t0.plus(i * 10, ChronoUnit.DAYS); // 10 days apart > 7-day window
            cultures.add(culture(cultureId, "p0", at));
            when(isolateRepository.findByCultureId(cultureId))
                    .thenReturn(List.of(isolate("wiso" + i, cultureId, "E. coli", at)));
            when(susceptibilityRepository.findByIsolateId("wiso" + i))
                    .thenReturn(List.of(new SusceptibilityResult("ws" + i, "wiso" + i,
                            "CIPROFLOXACIN", "S", null)));
        }
        when(cultureRepository.findAll()).thenReturn(cultures);
        when(drugRepository.findById("CIPROFLOXACIN"))
                .thenReturn(Optional.of(drug("CIPROFLOXACIN", "CIPROFLOXACIN", "Ciprofloxacin")));

        var report = service.report();
        assertThat(report.rows().get(0).isolates()).isEqualTo(2);
    }
}
