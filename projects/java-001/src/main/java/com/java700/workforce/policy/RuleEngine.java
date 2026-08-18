package com.java700.workforce.policy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.workforce.common.api.Problems;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Parses versioned rule definitions and dispatches them to typed evaluators.
 * Rule JSON: [{"type":"SOD_CONFLICT","severity":"HIGH","params":{...}}, ...]
 */
@Component
public class RuleEngine {

    private final ObjectMapper mapper;
    private final Map<RuleType, RuleEvaluator> evaluators = new EnumMap<>(RuleType.class);

    public RuleEngine(ObjectMapper mapper, List<RuleEvaluator> evaluatorBeans) {
        this.mapper = mapper;
        evaluatorBeans.forEach(e -> evaluators.put(e.type(), e));
    }

    public List<RuleDefinition> parse(String policyCode, String rulesJson) {
        try {
            List<JsonNode> nodes = mapper.readValue(rulesJson, new TypeReference<List<JsonNode>>() {
            });
            return nodes.stream().map(n -> toDefinition(policyCode, n)).toList();
        } catch (Exception e) {
            throw new Problems.BadRequest("Malformed policy rules JSON: " + e.getMessage());
        }
    }

    private RuleDefinition toDefinition(String policyCode, JsonNode node) {
        RuleType type = RuleType.valueOf(node.path("type").asText());
        Severity severity = Severity.valueOf(node.path("severity").asText("MEDIUM"));
        JsonNode params = node.path("params");
        return new RuleDefinition(policyCode, type, severity, params);
    }

    public RuleEvaluator evaluatorFor(RuleType type) {
        RuleEvaluator e = evaluators.get(type);
        if (e == null) {
            throw new Problems.BadRequest("No evaluator registered for rule type " + type);
        }
        return e;
    }

    public enum Severity {
        LOW, MEDIUM, HIGH
    }

    public record RuleDefinition(String policyCode, RuleType type, Severity severity, JsonNode params) {
    }
}
