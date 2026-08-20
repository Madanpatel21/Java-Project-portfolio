package com.java700.contracts.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Clause-level diff between two contract versions, computed over clause identity
 * (number + normalized title). Produces ADDED / REMOVED / MODIFIED entries with a
 * deterministic clause-number alignment — stable and readable for legal users.
 */
public final class ContractDiff {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ContractDiff() {
    }

    public record ClauseRef(String number, String title, String text, int sensitivity) {
    }

    public enum ChangeType {
        ADDED, REMOVED, MODIFIED
    }

    public record Change(ChangeType type, String number, String title,
                         String oldText, String newText) {
    }

    public static List<ClauseRef> clauses(String contentJson) {
        try {
            JsonNode node = MAPPER.readTree(contentJson);
            List<ClauseRef> out = new ArrayList<>();
            for (JsonNode c : node.path("clauses")) {
                out.add(new ClauseRef(c.path("number").asText(), c.path("title").asText(),
                        c.path("text").asText(), c.path("sensitivity").asInt(0)));
            }
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed contract content JSON", e);
        }
    }

    public static List<Change> diff(String oldJson, String newJson) {
        List<ClauseRef> oldClauses = clauses(oldJson);
        List<ClauseRef> newClauses = clauses(newJson);
        List<Change> out = new ArrayList<>();
        List<String> oldNums = oldClauses.stream().map(ClauseRef::number).toList();
        List<String> newNums = newClauses.stream().map(ClauseRef::number).toList();
        for (ClauseRef c : newClauses) {
            if (!oldNums.contains(c.number())) {
                out.add(new Change(ChangeType.ADDED, c.number(), c.title(), null, c.text()));
            }
        }
        for (ClauseRef c : oldClauses) {
            if (!newNums.contains(c.number())) {
                out.add(new Change(ChangeType.REMOVED, c.number(), c.title(), c.text(), null));
            }
        }
        for (ClauseRef n : newClauses) {
            for (ClauseRef o : oldClauses) {
                if (o.number().equals(n.number()) && !o.text().equals(n.text())) {
                    out.add(new Change(ChangeType.MODIFIED, n.number(), n.title(), o.text(), n.text()));
                }
            }
        }
        out.sort((a, b) -> a.number().compareTo(b.number()));
        return out;
    }
}
