package com.java700.expfraud.service;

/** Least-disclosure masking for employee identity fields. */
public final class MaskingUtil {

    private MaskingUtil() {
    }

    /** Masks all but the first character of each name part: "Priya Patel" -&gt; "P••••• P••••". */
    public static String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "••••";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder masked = new StringBuilder();
        for (String part : parts) {
            if (masked.length() > 0) {
                masked.append(' ');
            }
            masked.append(part.charAt(0));
            masked.append("••••".repeat(Math.max(1, part.length() - 1)));
        }
        return masked.toString();
    }
}
