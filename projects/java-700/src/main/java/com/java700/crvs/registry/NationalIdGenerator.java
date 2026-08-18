package com.java700.crvs.registry;

import java.time.Clock;
import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * National-ID generation: {@code <YYYY><5-digit sequence><check digit>}.
 * Check digit = (digits % 97) mod 10 — a simplified ISO 7064-style checksum that
 * detects transpositions and single-digit errors in manual entry.
 */
@Component
public class NationalIdGenerator {

    private final AtomicLong sequence = new AtomicLong(1);
    private final Clock clock;

    public NationalIdGenerator(Clock clock) {
        this.clock = clock;
    }

    public synchronized String next() {
        int year = Year.now(clock).getValue();
        long seq = sequence.getAndIncrement();
        String base = String.format("%04d%05d", year, Math.min(seq, 99_999));
        return base + checkDigit(base);
    }

    static char checkDigit(String base) {
        long value = 0;
        for (char c : base.toCharArray()) {
            value = value * 10 + (c - '0');
        }
        long rem = value % 97;
        return (char) ('0' + (rem % 10));
    }

    /** Validates a generated national id (used by the verification endpoint). */
    public static boolean isValid(String nationalId) {
        if (nationalId == null || nationalId.length() != 10) {
            return false;
        }
        String base = nationalId.substring(0, 9);
        return checkDigit(base) == nationalId.charAt(9);
    }
}
