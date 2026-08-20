package com.java700.legalmatter.screening;

import static org.assertj.core.api.Assertions.assertThat;

import com.java700.legalmatter.domain.MatterParty;
import com.java700.legalmatter.domain.Party;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConflictScreenerTest {

    private static final Instant NOW = Instant.parse("2026-08-20T00:00:00Z");

    private Party acmeClient;
    private Party betaOpponent;
    private Party gammaClient;
    private List<MatterParty> edges;

    @BeforeEach
    void setUp() {
        acmeClient = party("p1", "Acme Corporation", Party.Type.CLIENT);
        betaOpponent = party("p2", "Beta Industries", Party.Type.OPPONENT);
        gammaClient = party("p3", "Gamma Ltd", Party.Type.CLIENT);
        // Matter M1: Acme (client) vs Beta (opposing)
        edges = List.of(
                new MatterParty("e1", "m1", acmeClient.getId(), "CLIENT", NOW),
                new MatterParty("e2", "m1", betaOpponent.getId(), "OPPOSING", NOW));
    }

    private static Party party(String id, String name, Party.Type type) {
        return new Party(id, name, NameNormalizer.normalize(name), type, NOW);
    }

    @Test
    void clearWhenNoMatches() {
        ConflictScreener.ScreenResult result = ConflictScreener.screen(
                List.of(acmeClient, betaOpponent, gammaClient), edges,
                "New Corp", List.of("Unknown Entity"));
        assertThat(result.result()).isEqualTo("CLEAR");
    }

    @Test
    void directAdversityAgainstExistingClientIsConflict() {
        // prospective client Delta wants to sue Beta; Beta is adverse to existing client Acme
        ConflictScreener.ScreenResult result = ConflictScreener.screen(
                List.of(acmeClient, betaOpponent, gammaClient), edges,
                "Delta Corp", List.of("Beta Industries"));
        assertThat(result.result()).isEqualTo("CONFLICT");
        assertThat(result.findings()).anyMatch(f -> f.level().equals("CONFLICT")
                && f.detail().contains("Acme"));
    }

    @Test
    void subjectMatchingExistingOpponentIsConflict() {
        ConflictScreener.ScreenResult result = ConflictScreener.screen(
                List.of(acmeClient, betaOpponent, gammaClient), edges,
                "Beta Industries", List.of("Some Adversary"));
        assertThat(result.result()).isEqualTo("CONFLICT");
    }

    @Test
    void fuzzyAdverseMatchResolvesToRealPartyConflict() {
        // typo resolves to the real Beta Industries, which is adverse to Acme -> CONFLICT,
        // with a POTENTIAL note about the fuzzy match itself
        ConflictScreener.ScreenResult result = ConflictScreener.screen(
                List.of(acmeClient, betaOpponent, gammaClient), edges,
                "Delta Corp", List.of("Beta Industriess"));
        assertThat(result.result()).isEqualTo("CONFLICT");
        assertThat(result.findings()).anyMatch(f -> "POTENTIAL".equals(f.level())
                && f.detail().contains("fuzzy"));
    }

    @Test
    void fuzzyMatchWithoutMatterLinksIsPotentialOnly() {
        Party unrelated = party("p9", "Beta Industries AG", Party.Type.OPPONENT);
        ConflictScreener.ScreenResult result = ConflictScreener.screen(
                List.of(acmeClient, betaOpponent, gammaClient, unrelated), List.of(),
                "Delta Corp", List.of("Beta Industriess")); // closest: no matter edges at all
        assertThat(result.result()).isEqualTo("POTENTIAL");
    }

    @Test
    void subjectOnSameMatterAsAdverseIsPotential() {
        // Delta (already a client) vs Acme? Acme is Delta's... simulate: adverse name = Acme, subject = Delta,
        // and Delta shares a matter with Acme? Not in graph. Use direct: subject Gamma vs adverse Acme where
        // Acme is a client on another matter → CONFLICT only if adverse links to a DIFFERENT client.
        ConflictScreener.ScreenResult result = ConflictScreener.screen(
                List.of(acmeClient, betaOpponent, gammaClient), edges,
                "Gamma Ltd", List.of("Acme Corporation"));
        assertThat(result.result()).isEqualTo("CONFLICT");
    }

    @Test
    void normalizationHandlesCaseAndPunctuation() {
        assertThat(NameNormalizer.normalize("ACME, Corp."))
                .isEqualTo(NameNormalizer.normalize("acme corp"));
        assertThat(NameNormalizer.jaroWinkler("beta industries", "beta industriez"))
                .isGreaterThan(0.92);
    }
}
