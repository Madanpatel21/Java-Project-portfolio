package com.java700.workforce.evidence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * Tamper-evident chaining. Each entry's hash = SHA-256(prevHash || canonicalJson(payload)).
 * Canonical JSON (sorted keys, no pretty-printing) makes the chain deterministic across
 * serializations; any mutation of any payload or link breaks verification from that entry onward.
 */
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
            throw new IllegalStateException("Cannot serialize evidence payload", e);
        }
    }

    public static String hash(String prevHash, String canonicalPayloadJson) {
        return sha256Hex(prevHash + "|" + canonicalPayloadJson);
    }

    /**
     * Verifies linkage, content and sequence contiguity of a consecutive list of entries.
     * Any payload mutation, link rewrite or sequence renumbering breaks verification
     * from that entry onward.
     */
    public static Verification verify(List<EvidenceEntry> entries) {
        String prev = GENESIS_PREV_HASH;
        Long expectedSeq = null;
        int index = 0;
        for (EvidenceEntry e : entries) {
            if (expectedSeq != null && e.getSeq() != expectedSeq + 1) {
                return new Verification(false, index, e.getSeq(), null, null);
            }
            expectedSeq = e.getSeq();
            String expected = hash(prev, e.getPayload());
            if (!expected.equals(e.getHash())) {
                return new Verification(false, index, e.getSeq(), expected, e.getHash());
            }
            if (!prev.equals(e.getPrevHash())) {
                return new Verification(false, index, e.getSeq(), prev, e.getPrevHash());
            }
            prev = e.getHash();
            index++;
        }
        return new Verification(true, entries.size(), null, null, null);
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
