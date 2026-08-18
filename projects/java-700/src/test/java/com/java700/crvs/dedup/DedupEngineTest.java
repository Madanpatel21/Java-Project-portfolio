package com.java700.crvs.dedup;

import static org.assertj.core.api.Assertions.assertThat;

import com.java700.crvs.registry.Person;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DedupEngineTest {

    private static final DedupEngine ENGINE = new DedupEngine(0.85);
    private static final Instant NOW = Instant.parse("2026-08-18T00:00:00Z");

    private static Person person(String id, String name, LocalDate dob, String sex,
                                 String place, String parents) {
        return new Person(id, "NID-" + id, name, dob, sex, place, parents, "NORTH", NOW);
    }

    @Test
    void identicalRecordsScorePerfectly() {
        Person a = person("a", "Ada Lovelace", LocalDate.of(1990, 1, 1), "F", "London",
                "Byron Lovelace, Anne Isabella");
        Person b = person("b", "Ada Lovelace", LocalDate.of(1990, 1, 1), "F", "London",
                "Byron Lovelace, Anne Isabella");
        assertThat(ENGINE.score(a, b)).isEqualTo(1.0);
    }

    @Test
    void blockingKeyExcludesDifferentDobOrSex() {
        Person a = person("a", "Ada Lovelace", LocalDate.of(1990, 1, 1), "F", "London", "");
        Person b = person("b", "Ada Lovelace", LocalDate.of(1991, 1, 1), "F", "London", "");
        assertThat(ENGINE.sameBlock(a, b)).isFalse();
        Person c = person("c", "Ada Lovelace", LocalDate.of(1990, 1, 1), "M", "London", "");
        assertThat(ENGINE.sameBlock(a, c)).isFalse();
    }

    @Test
    void typoInNameScoresHighButBelowIdentity() {
        // single-letter typo → Jaro-Winkler very high but not 1.0
        double score = ENGINE.score(
                person("a", "Ada Lovelace", LocalDate.of(1990, 1, 1), "F", "London",
                        "Byron Lovelace, Anne Isabella"),
                person("b", "Ada Lovelacee", LocalDate.of(1990, 1, 1), "F", "London",
                        "Byron Lovelace, Anne Isabella"));
        assertThat(score).isGreaterThan(0.9);
        assertThat(score).isLessThan(1.0);
    }

    @Test
    void differentNamesScoreLow() {
        // same place + neutral parents give a floor of ~0.35; different names stay well below threshold
        double score = ENGINE.score(
                person("a", "Ada Lovelace", LocalDate.of(1990, 1, 1), "F", "London", ""),
                person("b", "Grace Hopper", LocalDate.of(1990, 1, 1), "F", "London", ""));
        assertThat(score).isLessThan(0.85);
        assertThat(score).isLessThan(ENGINE.score(
                person("c", "Ada Lovelace", LocalDate.of(1990, 1, 1), "F", "London", ""),
                person("d", "Ada Lovelacee", LocalDate.of(1990, 1, 1), "F", "London", "")));
    }

    @Test
    void findCandidatesRaisesOnlyAboveThreshold() {
        Person subject = person("s", "Ada Lovelace", LocalDate.of(1990, 1, 1), "F", "London",
                "Byron Lovelace, Anne Isabella");
        Person dup = person("d", "Ada Lovelacee", LocalDate.of(1990, 1, 1), "F", "London",
                "Byron Lovelace, Anne Isabella");
        Person unrelated = person("u", "Grace Hopper", LocalDate.of(1990, 1, 1), "F", "London", "");
        List<DedupEngine.ScoredPair> candidates =
                ENGINE.findCandidates(List.of(subject, dup, unrelated), subject);
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).personBId()).isEqualTo("d");
    }

    @Test
    void normalizeStripsPunctuationAndCase() {
        assertThat(DedupEngine.normalize("  Ada,  Lovelace! ")).isEqualTo("ada lovelace");
    }

    @Test
    void jaroWinklerBoundaries() {
        assertThat(DedupEngine.jaroWinkler("abc", "abc")).isEqualTo(1.0);
        assertThat(DedupEngine.jaroWinkler("abc", "xyz")).isEqualTo(0.0);
        assertThat(DedupEngine.jaroWinkler("", "")).isEqualTo(1.0);
    }
}
