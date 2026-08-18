package com.java700.crvs.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HashChainTest {

    private static LifeEvent event(long globalSeq, String personId, String payload,
                                   String prevGlobal, String globalHash,
                                   long chainSeq, String prevChain, String chainHash) {
        return new LifeEvent(globalSeq, personId, "BIRTH", payload, "tester", Instant.now(),
                prevGlobal, globalHash, chainSeq, prevChain, chainHash);
    }

    private static List<LifeEvent> globalChain(int n) {
        List<LifeEvent> out = new ArrayList<>();
        String prev = HashChain.GENESIS_PREV_HASH;
        for (int i = 1; i <= n; i++) {
            String payload = HashChain.canonicalJson(Map.of("i", i));
            String hash = HashChain.hash(prev, payload);
            out.add(event(i, "p" + (i % 3), payload, prev, hash, i, prev, hash));
            prev = hash;
        }
        return out;
    }

    @Test
    void cleanGlobalChainVerifies() {
        assertThat(HashChain.verify(globalChain(12)).valid()).isTrue();
    }

    @Test
    void tamperedPayloadBreaksAtEntry() {
        List<LifeEvent> events = globalChain(12);
        LifeEvent victim = events.get(6);
        events.set(6, event(victim.getGlobalSeq(), victim.getPersonId(), "{\"tampered\":true}",
                victim.getPrevGlobalHash(), victim.getGlobalHash(),
                victim.getChainSeq(), victim.getPrevChainHash(), victim.getChainHash()));
        HashChain.Verification v = HashChain.verify(events);
        assertThat(v.valid()).isFalse();
        assertThat(v.brokenSeq()).isEqualTo(7L);
    }

    @Test
    void sequenceGapDetected() {
        List<LifeEvent> events = globalChain(10);
        LifeEvent last = events.get(9);
        events.set(9, event(99L, last.getPersonId(), last.getPayload(),
                last.getPrevGlobalHash(), last.getGlobalHash(),
                last.getChainSeq(), last.getPrevChainHash(), last.getChainHash()));
        HashChain.Verification v = HashChain.verify(events);
        assertThat(v.valid()).isFalse();
        assertThat(v.brokenSeq()).isEqualTo(99L);
    }

    @Test
    void perPersonChainIsIndependentlyVerifiable() {
        // build a person chain of 4 events
        List<LifeEvent> personChain = new ArrayList<>();
        String prev = HashChain.GENESIS_PREV_HASH;
        for (int i = 1; i <= 4; i++) {
            String payload = HashChain.canonicalJson(Map.of("lifeEvent", i));
            String hash = HashChain.hash(prev, payload);
            personChain.add(event(100L + i, "person-1", payload, "0".repeat(64),
                    hash, i, prev, hash));
            prev = hash;
        }
        assertThat(HashChain.verifyPerson(personChain).valid()).isTrue();
        // tamper event 2
        LifeEvent victim = personChain.get(1);
        personChain.set(1, event(victim.getGlobalSeq(), victim.getPersonId(), "{\"x\":1}",
                victim.getPrevGlobalHash(), victim.getGlobalHash(),
                victim.getChainSeq(), victim.getPrevChainHash(), victim.getChainHash()));
        assertThat(HashChain.verifyPerson(personChain).valid()).isFalse();
    }

    @Test
    void canonicalJsonIsDeterministic() {
        assertThat(HashChain.canonicalJson(Map.of("b", 2, "a", 1)))
                .isEqualTo(HashChain.canonicalJson(Map.of("a", 1, "b", 2)));
    }
}
