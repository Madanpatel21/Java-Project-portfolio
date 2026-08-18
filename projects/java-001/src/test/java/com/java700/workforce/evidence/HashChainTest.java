package com.java700.workforce.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HashChainTest {

    private static EvidenceEntry entry(long seq, String payload, String prevHash, String hash) {
        return new EvidenceEntry(seq, "TEST", "agg-" + seq, "EVENT", "tester", payload, prevHash, hash,
                Instant.now());
    }

    private static List<EvidenceEntry> chain(int n) {
        List<EvidenceEntry> out = new ArrayList<>();
        String prev = HashChain.GENESIS_PREV_HASH;
        for (int i = 1; i <= n; i++) {
            String payload = HashChain.canonicalJson(Map.of("i", i, "note", "entry " + i));
            String hash = HashChain.hash(prev, payload);
            out.add(entry(i, payload, prev, hash));
            prev = hash;
        }
        return out;
    }

    @Test
    void hashingIsDeterministicAndCanonical() {
        String a = HashChain.canonicalJson(Map.of("b", 2, "a", 1));
        String b = HashChain.canonicalJson(Map.of("a", 1, "b", 2));
        assertThat(a).isEqualTo(b);
        assertThat(HashChain.hash(HashChain.GENESIS_PREV_HASH, a))
                .isEqualTo(HashChain.hash(HashChain.GENESIS_PREV_HASH, b));
    }

    @Test
    void validChainVerifies() {
        assertThat(HashChain.verify(chain(10)).valid()).isTrue();
    }

    @Test
    void tamperedPayloadBreaksChainAtEntry() {
        List<EvidenceEntry> entries = chain(10);
        EvidenceEntry victim = entries.get(5);
        entries.set(5, entry(victim.getSeq(), "{\"tampered\":true}", victim.getPrevHash(), victim.getHash()));
        HashChain.Verification v = HashChain.verify(entries);
        assertThat(v.valid()).isFalse();
        assertThat(v.brokenSeq()).isEqualTo(6L);
    }

    @Test
    void tamperedLinkBreaksChain() {
        List<EvidenceEntry> entries = chain(10);
        EvidenceEntry victim = entries.get(4);
        entries.set(4, entry(victim.getSeq(), victim.getPayload(), "f".repeat(64), victim.getHash()));
        HashChain.Verification v = HashChain.verify(entries);
        assertThat(v.valid()).isFalse();
        assertThat(v.brokenSeq()).isEqualTo(5L);
    }

    @Test
    void genesisHashIsZeroFilled() {
        assertThat(HashChain.GENESIS_PREV_HASH).hasSize(64).containsOnlyDigits().isEqualTo("0".repeat(64));
    }
}
