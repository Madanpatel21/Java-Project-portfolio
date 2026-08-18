package com.java700.crvs.registry;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class NationalIdGeneratorTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-18T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void generatesTenDigitIdWithValidChecksum() {
        NationalIdGenerator generator = new NationalIdGenerator(clock);
        String id = generator.next();
        assertThat(id).hasSize(10);
        assertThat(id).startsWith("2026");
        assertThat(NationalIdGenerator.isValid(id)).isTrue();
    }

    @Test
    void singleDigitErrorDetected() {
        NationalIdGenerator generator = new NationalIdGenerator(clock);
        String id = generator.next();
        char last = id.charAt(9);
        char tampered = last == '9' ? '0' : (char) (last + 1);
        String forged = id.substring(0, 9) + tampered;
        if (forged.equals(id)) {
            forged = id.substring(0, 9) + '0';
        }
        assertThat(NationalIdGenerator.isValid(forged)).isFalse();
    }

    @Test
    void malformedIdsRejected() {
        assertThat(NationalIdGenerator.isValid(null)).isFalse();
        assertThat(NationalIdGenerator.isValid("123")).isFalse();
        assertThat(NationalIdGenerator.isValid("abcdefghij")).isFalse();
    }

    @Test
    void sequenceIncrements() {
        NationalIdGenerator generator = new NationalIdGenerator(clock);
        String a = generator.next();
        String b = generator.next();
        assertThat(a).isNotEqualTo(b);
        assertThat(Long.parseLong(b.substring(4, 9))).isGreaterThan(Long.parseLong(a.substring(4, 9)));
    }
}
