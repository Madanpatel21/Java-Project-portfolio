package com.java700.wflow.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsed, immutable view of a workflow definition JSON:
 * nodes (START, APPROVAL, AUTOMATED, GATEWAY, TIMER, END) with edges,
 * gateway conditions and optional compensation nodes.
 */
public final class WorkflowModel {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<Node> nodes;
    private final String startId;

    private WorkflowModel(List<Node> nodes, String startId) {
        this.nodes = nodes;
        this.startId = startId;
    }

    public record Node(String id, String type, String next, String assigneeRole,
                       int slaHours, int timerHours, String compensationNode,
                       List<Condition> conditions) {

        public Node {
            conditions = List.copyOf(conditions);
        }
    }

    public record Condition(String expression, String next, boolean isDefault) {
    }

    public static WorkflowModel parse(String definitionJson) {
        try {
            JsonNode root = MAPPER.readTree(definitionJson);
            List<Node> nodes = new ArrayList<>();
            String start = null;
            for (JsonNode n : root.path("nodes")) {
                String id = n.path("id").asText();
                String type = n.path("type").asText().toUpperCase();
                if ("START".equals(type)) {
                    start = id;
                }
                List<Condition> conditions = new ArrayList<>();
                for (JsonNode c : n.path("conditions")) {
                    conditions.add(new Condition(c.path("expr").asText(""),
                            c.path("next").asText(), c.path("default").asBoolean(false)));
                }
                nodes.add(new Node(id, type, n.path("next").asText(""),
                        n.path("role").asText(""), n.path("slaHours").asInt(24),
                        n.path("timerHours").asInt(0), n.path("compensation").asText(""),
                        conditions));
            }
            if (start == null) {
                throw new IllegalArgumentException("Definition has no START node");
            }
            return new WorkflowModel(nodes, start);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed workflow definition JSON", e);
        }
    }

    public String startId() {
        return startId;
    }

    public Node node(String id) {
        return nodes.stream().filter(n -> n.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown node: " + id));
    }

    public List<Node> nodes() {
        return List.copyOf(nodes);
    }
}
