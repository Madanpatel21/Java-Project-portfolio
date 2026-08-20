package com.java700.govault.service;

import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * Lightweight text extraction for indexing (txt/md/csv). In the production profile
 * this is replaced by Apache Tika for PDF/DOCX/XLSX support (documented in ADR-0002).
 */
@Component
public class TextExtractor {

    public String extract(String fileName, byte[] content) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".csv")
                || lower.endsWith(".json") || lower.endsWith(".xml") || lower.endsWith(".log")) {
            return new String(content, StandardCharsets.UTF_8).substring(
                    0, Math.min(8000, content.length));
        }
        return "";
    }
}
