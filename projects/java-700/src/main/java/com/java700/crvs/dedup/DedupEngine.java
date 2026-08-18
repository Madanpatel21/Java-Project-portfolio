package com.java700.crvs.dedup;

import com.java700.crvs.registry.Person;
import java.util.ArrayList;
import java.util.List;

/**
 * Fuzzy duplicate detection for identity records.
 *
 * <p>Blocking key: sex + date of birth + first character of the normalized name.
 * Scoring (0..1): 0.5 × name similarity (Jaro-Winkler over normalized names)
 * + 0.3 × parent-name overlap + 0.2 × place-of-birth match. Candidates at or above
 * the configured threshold are raised for adjudication.</p>
 */
public final class DedupEngine {

    private final double threshold;

    public DedupEngine(double threshold) {
        this.threshold = threshold;
    }

    /** Scores all same-block pairs of the registry snapshot above the threshold. */
    public List<ScoredPair> findCandidates(List<Person> snapshot, Person subject) {
        List<ScoredPair> out = new ArrayList<>();
        for (Person other : snapshot) {
            if (other.getId().equals(subject.getId())) {
                continue;
            }
            if (!sameBlock(subject, other)) {
                continue;
            }
            double score = score(subject, other);
            if (score >= threshold) {
                out.add(new ScoredPair(subject.getId(), other.getId(), round4(score)));
            }
        }
        return out;
    }

    public double score(Person a, Person b) {
        double name = jaroWinkler(normalize(a.getFullName()), normalize(b.getFullName()));
        double parents = parentOverlap(a.getParentNames(), b.getParentNames());
        double place = normalize(a.getPlaceOfBirth()).equals(normalize(b.getPlaceOfBirth())) ? 1.0 : 0.0;
        return 0.5 * name + 0.3 * parents + 0.2 * place;
    }

    public boolean sameBlock(Person a, Person b) {
        return a.getSex().equals(b.getSex())
                && a.getDob().equals(b.getDob())
                && normalize(a.getFullName()).charAt(0) == normalize(b.getFullName()).charAt(0);
    }

    static String normalize(String s) {
        if (s == null) {
            return "";
        }
        return s.toLowerCase()
                .replaceAll("[^a-z ]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    static double parentOverlap(String parentsA, String parentsB) {
        List<String> a = tokens(parentsA);
        List<String> b = tokens(parentsB);
        if (a.isEmpty() && b.isEmpty()) {
            return 0.5; // no parental data: neutral
        }
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        long shared = a.stream().filter(t -> b.stream().anyMatch(x -> x.equals(t)
                || jaroWinkler(t, x) > 0.9)).count();
        return (double) shared / Math.max(a.size(), b.size());
    }

    private static List<String> tokens(String s) {
        if (s == null || s.isBlank()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String part : s.split(",")) {
            String t = normalize(part);
            if (!t.isBlank()) {
                out.add(t);
            }
        }
        return out;
    }

    /** Standard Jaro-Winkler similarity in [0,1]. */
    static double jaroWinkler(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        int matchDistance = Math.max(a.length(), b.length()) / 2 - 1;
        matchDistance = Math.max(matchDistance, 0);
        boolean[] aMatches = new boolean[a.length()];
        boolean[] bMatches = new boolean[b.length()];
        int matches = 0;
        for (int i = 0; i < a.length(); i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, b.length());
            for (int j = start; j < end; j++) {
                if (!bMatches[j] && a.charAt(i) == b.charAt(j)) {
                    aMatches[i] = true;
                    bMatches[j] = true;
                    matches++;
                    break;
                }
            }
        }
        if (matches == 0) {
            return 0.0;
        }
        int transpositions = 0;
        int k = 0;
        for (int i = 0; i < a.length(); i++) {
            if (aMatches[i]) {
                while (!bMatches[k]) {
                    k++;
                }
                if (a.charAt(i) != b.charAt(k)) {
                    transpositions++;
                }
                k++;
            }
        }
        double jaro = ((double) matches / a.length()
                + (double) matches / b.length()
                + (double) (matches - transpositions / 2) / matches) / 3.0;
        int prefix = 0;
        for (int i = 0; i < Math.min(4, Math.min(a.length(), b.length())); i++) {
            if (a.charAt(i) == b.charAt(i)) {
                prefix++;
            } else {
                break;
            }
        }
        return jaro + prefix * 0.1 * (1 - jaro);
    }

    private static double round4(double v) {
        return Math.round(v * 10_000.0) / 10_000.0;
    }

    public record ScoredPair(String personAId, String personBId, double score) {
    }
}
