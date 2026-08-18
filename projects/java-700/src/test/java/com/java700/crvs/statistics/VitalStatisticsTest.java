package com.java700.crvs.statistics;

import com.java700.crvs.registry.PersonRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.java700.crvs.ledger.LifeEvent;
import com.java700.crvs.ledger.LifeEventRepository;
import com.java700.crvs.registry.Person;
import com.java700.crvs.registry.PersonRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VitalStatisticsTest {

    private static final Instant FROM = Instant.parse("2026-08-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-08-19T00:00:00Z");

    private static LifeEvent event(String personId, String type, Instant at) {
        return new LifeEvent((long) at.hashCode() & 0x7FFFFFFF, personId, type, "{}", "sys",
                at, "0".repeat(64), "a".repeat(64), 1L, "0".repeat(64), "b".repeat(64));
    }

    private VitalStatistics service(List<LifeEvent> events) {
        LifeEventRepository ledger = mock(LifeEventRepository.class);
        when(ledger.findAll()).thenReturn(events);
        PersonRepository persons = mock(PersonRepository.class);
        when(persons.findById("p1")).thenReturn(Optional.of(person("p1", "NORTH")));
        when(persons.findById("p2")).thenReturn(Optional.of(person("p2", "NORTH")));
        when(persons.findById("p3")).thenReturn(Optional.of(person("p3", "SOUTH")));
        return new VitalStatistics(ledger, persons);
    }

    private static Person person(String id, String region) {
        return new Person(id, "NID-" + id, "Name " + id, java.time.LocalDate.of(1990, 1, 1),
                "F", "Place", null, region, Instant.now());
    }

    @Test
    void aggregatesByRegionWithNaturalIncrease() {
        var service = service(List.of(
                event("p1", "BIRTH", Instant.parse("2026-08-02T00:00:00Z")),
                event("p2", "BIRTH", Instant.parse("2026-08-03T00:00:00Z")),
                event("p1", "DEATH", Instant.parse("2026-08-10T00:00:00Z")),
                event("p3", "BIRTH", Instant.parse("2026-08-11T00:00:00Z")),
                event("p1", "MARRIAGE", Instant.parse("2026-08-12T00:00:00Z"))));
        VitalStatistics.VitalReport report = service.report(FROM, TO);
        assertThat(report.births()).isEqualTo(3);
        assertThat(report.deaths()).isEqualTo(1);
        assertThat(report.marriages()).isEqualTo(1);
        assertThat(report.naturalIncrease()).isEqualTo(2);
        assertThat(report.regions()).hasSize(2);
        var north = report.regions().stream().filter(r -> r.region().equals("NORTH")).findFirst().orElseThrow();
        assertThat(north.births()).isEqualTo(2);
        assertThat(north.deaths()).isEqualTo(1);
        assertThat(north.naturalIncrease()).isEqualTo(1);
        var south = report.regions().stream().filter(r -> r.region().equals("SOUTH")).findFirst().orElseThrow();
        assertThat(south.births()).isEqualTo(1);
    }

    @Test
    void windowFiltersEventsOutsideRange() {
        var service = service(List.of(
                event("p1", "BIRTH", Instant.parse("2026-07-30T00:00:00Z")),
                event("p2", "BIRTH", Instant.parse("2026-08-05T00:00:00Z")),
                event("p3", "BIRTH", Instant.parse("2026-08-20T00:00:00Z"))));
        VitalStatistics.VitalReport report = service.report(FROM, TO);
        assertThat(report.births()).isEqualTo(1);
    }

    @Test
    void invalidWindowRejected() {
        var service = service(List.of());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.report(TO, FROM))
                .isInstanceOf(com.java700.crvs.common.api.Problems.BadRequest.class);
    }
}
