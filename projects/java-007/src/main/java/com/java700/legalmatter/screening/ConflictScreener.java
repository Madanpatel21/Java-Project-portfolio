package com.java700.legalmatter.screening;

import com.java700.legalmatter.domain.MatterParty;
import com.java700.legalmatter.domain.Party;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure conflict-screening over a snapshot of the parties graph.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>Match each adverse name against existing parties (exact normalized, else fuzzy ≥ 0.92 → POTENTIAL).</li>
 *   <li>If the subject matches an existing OPPONENT/RELATED party → CONFLICT.</li>
 *   <li>Graph walk: for every matched adverse party, find matters it appears on; if any such matter
 *       has a CLIENT party different from the subject → direct adversity → CONFLICT.</li>
 *   <li>Adverse party RELATED to an existing client on any matter → POTENTIAL.</li>
 * </ol>
 */
public final class ConflictScreener {

    public static final double FUZZY_THRESHOLD = 0.92;

    public record Finding(String level, String detail) {
    }

    public record ScreenResult(String result, List<Finding> findings) {

        public ScreenResult {
            findings = List.copyOf(findings);
        }
    }

    private ConflictScreener() {
    }

    public static ScreenResult screen(List<Party> parties, List<MatterParty> edges,
                                      String subjectName, List<String> adverseNames) {
        String subject = NameNormalizer.normalize(subjectName);
        List<Finding> findings = new ArrayList<>();
        Map<String, Party> byId = new HashMap<>();
        for (Party p : parties) {
            byId.put(p.getId(), p);
        }
        Map<String, List<MatterParty>> byParty = new HashMap<>();
        Map<String, List<MatterParty>> byMatter = new HashMap<>();
        for (MatterParty edge : edges) {
            byParty.computeIfAbsent(edge.getPartyId(), k -> new ArrayList<>()).add(edge);
            byMatter.computeIfAbsent(edge.getMatterId(), k -> new ArrayList<>()).add(edge);
        }
        // subject vs existing parties
        for (Party existing : parties) {
            if (existing.getType() != Party.Type.CLIENT
                    && NameNormalizer.jaroWinkler(subject, existing.getNormalizedName())
                            >= FUZZY_THRESHOLD) {
                findings.add(new Finding("CONFLICT",
                        "Subject '" + subjectName + "' matches existing "
                                + existing.getType().name().toLowerCase() + " party '"
                                + existing.getName() + "'"));
            }
        }
        // adverse names
        for (String adverse : adverseNames) {
            String normalized = NameNormalizer.normalize(adverse);
            List<Party> matched = parties.stream()
                    .filter(p -> p.getNormalizedName().equals(normalized))
                    .toList();
            boolean fuzzy = false;
            if (matched.isEmpty()) {
                matched = parties.stream()
                        .filter(p -> NameNormalizer.jaroWinkler(normalized, p.getNormalizedName())
                                >= FUZZY_THRESHOLD)
                        .toList();
                fuzzy = !matched.isEmpty();
            }
            if (matched.isEmpty()) {
                continue;
            }
            if (fuzzy) {
                findings.add(new Finding("POTENTIAL",
                        "Adverse name '" + adverse + "' fuzzy-matches existing party '"
                                + matched.get(0).getName() + "'"));
            }
            for (Party m : matched) {
                for (MatterParty edge : byParty.getOrDefault(m.getId(), List.of())) {
                    String clientId = null;
                    String clientName = null;
                    for (MatterParty e : byMatter.getOrDefault(edge.getMatterId(), List.of())) {
                        if ("CLIENT".equals(e.getRole())) {
                            clientId = e.getPartyId();
                            clientName = byId.get(clientId) == null ? "?" : byId.get(clientId).getName();
                        }
                    }
                    if (clientId != null && !subject.equals(byId.get(clientId) == null
                            ? "" : byId.get(clientId).getNormalizedName())) {
                        findings.add(new Finding("CONFLICT",
                                "Adverse party '" + adverse + "' appears on a matter with existing client '"
                                        + clientName + "'"));
                    } else if (clientId != null) {
                        findings.add(new Finding("POTENTIAL",
                                "Adverse party '" + adverse + "' is RELATED to the subject on an existing matter"));
                    }
                }
            }
        }
        boolean conflict = findings.stream().anyMatch(f -> "CONFLICT".equals(f.level()));
        boolean potential = findings.stream().anyMatch(f -> "POTENTIAL".equals(f.level()));
        String result = conflict ? "CONFLICT" : potential ? "POTENTIAL" : "CLEAR";
        return new ScreenResult(result, findings);
    }
}
