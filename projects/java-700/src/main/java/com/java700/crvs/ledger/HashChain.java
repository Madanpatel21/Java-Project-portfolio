package com.java700.crvs.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/** Deterministic SHA-256 chaining over canonical JSON payloads. */
public final class HashChain {

    public static final String GENESIS_PREV_HASH = "0".repeat(64);

    private static final ObjectMapper CANONICAL = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .configure(SerializationFeature.INDENT_OUTPUT, false);

    private HashChain() {
    }

    public static String canonicalJson(Object payload) {
        try {
            return CANONICAL.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize ledger payload", e);
        }
    }

    public static String hash(String prevHash, String canonicalPayloadJson) {
        return sha256Hex(prevHash + "|" + canonicalPayloadJson);
    }

    /** Verifies a consecutive list of global-chain links (linkage, content, sequence contiguity). */
    public static Verification verify(List<LifeEvent> events) {
        String prev = GENESIS_PREV_HASH;
        Long expectedSeq = null;
        int index = 0;
        for (LifeEvent e : events) {
            if (expectedSeq != null && e.getGlobalSeq() != expectedSeq + 1) {
                return new Verification(false, index, e.getGlobalSeq(), null, null);
            }
            expectedSeq = e.getGlobalSeq();
            String expected = hash(prev, e.getPayload());
            if (!expected.equals(e.getGlobalHash())) {
                return new Verification(false, index, e.getGlobalSeq(), expected, e.getGlobalHash());
            }
            if (!prev.equals(e.getPrevGlobalHash())) {
                return new Verification(false, index, e.getGlobalSeq(), prev, e.getPrevGlobalHash());
            }
            prev = e.getGlobalHash();
            index++;
        }
        return new Verification(true, events.size(), null, null, null);
    }

    /** Verifies one person's per-person chain. */
    public static Verification verifyPerson(List<LifeEvent> events) {
        String prev = GENESIS_PREV_HASH;
        long expectedChainSeq = 0;
        int index = 0;
        for (LifeEvent e : events) {
            expectedChainSeq++;
            if (e.getChainSeq() != expectedChainSeq) {
                return new Verification(false, index, e.getGlobalSeq(), null, null);
            }
            String expected = hash(prev, e.getPayload());
            if (!expected.equals(e.getChainHash())) {
                return new Verification(false, index, e.getGlobalSeq(), expected, e.getChainHash());
            }
            if (!prev.equals(e.getPrevChainHash())) {
                return new Verification(false, index, e.getGlobalSeq(), prev, e.getPrevChainHash());
            }
            prev = e.getChainHash();
            index++;
        }
        return new Verification(true, events.size(), null, null, null);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public record Verification(boolean valid, int entriesChecked, Long brokenSeq,
                               String expectedHash, String actualHash) {
    }
}
