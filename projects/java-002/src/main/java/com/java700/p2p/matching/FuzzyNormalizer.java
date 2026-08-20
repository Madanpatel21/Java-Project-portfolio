package com.java700.p2p.matching;

/** Normalization helpers for supplier names and item codes across heterogeneous sources. */
public final class FuzzyNormalizer {

    private FuzzyNormalizer() {
    }

    /** Uppercase, trim, strip punctuation/spacing variants: "Widget-2000" == "widget 2000". */
    public static String itemCode(String s) {
        if (s == null) {
            return "";
        }
        return s.toUpperCase()
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }

    /** Lowercase, strip punctuation, collapse whitespace: supplier names. */
    public static String supplier(String s) {
        if (s == null) {
            return "";
        }
        return s.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /** Jaro-Winkler similarity in [0,1] — used for supplier-name fuzzy matching. */
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
