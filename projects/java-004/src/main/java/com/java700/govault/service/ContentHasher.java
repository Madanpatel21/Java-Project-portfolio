package com.java700.govault.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/** SHA-256 content hashing — the fingerprint recorded at upload and in disposition proofs. */
@Component
public class ContentHasher {

    public String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public String sha256(String text) {
        return sha256(text.getBytes(StandardCharsets.UTF_8));
    }
}
