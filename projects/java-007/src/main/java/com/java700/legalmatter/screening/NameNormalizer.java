package com.java700.legalmatter.screening;

/** Normalization for party-name matching: case, punctuation and whitespace variants collapse. */
public final class NameNormalizer {

    private NameNormalizer() {
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static double jaroWinkler(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        int matchDistance = Math.max(Math.max(a.length(), b.length()) / 2 - 1, 0);
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
}
