package com.java700.wflow.engine;

import com.java700.wflow.engine.WorkflowModel.Node;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Pure, deterministic workflow interpreter. Advances an instance one step at a time and
 * returns a plan of actions for the service to persist. No I/O — fully unit-testable.
 */
@Component
public class WorkflowEngine {

    public enum ActionType {
        CREATE_TASK, SET_TIMER, RECORD_STEP, COMPLETE, UPDATE_VARS
    }

    public record Action(ActionType type, String nodeId, String assigneeRole, int slaHours,
                         Instant resumeAt, String resultJson, Map<String, Object> vars) {

        public Action {
            vars = vars == null ? Map.of() : Map.copyOf(vars);
        }
    }

    public record AdvanceResult(String status, String currentNodeId, List<Action> actions) {

        public AdvanceResult {
            actions = List.copyOf(actions);
        }
    }

    public WorkflowEngine() {
    }

    /**
     * Advances from {@code currentStatus}/{@code currentNodeId}. WAITING_TASK means a task
     * completed (its result is in {@code taskResult}); WAITING_TIMER means the timer fired.
     */
    public AdvanceResult advance(WorkflowModel model, String currentStatus,
                                 String currentNodeId, Map<String, Object> vars,
                                 Map<String, Object> taskResult, Instant now) {
        List<Action> actions = new ArrayList<>();
        String status = currentStatus;
        String current = currentNodeId;

        if ("WAITING_TASK".equals(status)) {
            Node node = model.node(current);
            actions.add(new Action(ActionType.RECORD_STEP, current, null, 0, null,
                    toJson(taskResult), null));
            if (taskResult != null && taskResult.containsKey("vars")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> merged = new HashMap<>(vars);
                merged.putAll((Map<String, Object>) taskResult.get("vars"));
                vars = merged;
                actions.add(new Action(ActionType.UPDATE_VARS, current, null, 0, null, null, vars));
            }
            if (node.compensationNode() != null && !node.compensationNode().isBlank()) {
                actions.add(new Action(ActionType.RECORD_STEP, node.compensationNode(), null, 0,
                        null, "{\"compensatedFor\":\"" + current + "\"}", null));
            }
            current = node.next();
            status = "RUNNING";
        } else if ("WAITING_TIMER".equals(status)) {
            actions.add(new Action(ActionType.RECORD_STEP, current, null, 0, null,
                    "{\"timer\":\"fired\"}", null));
            current = model.node(current).next();
            status = "RUNNING";
        } else if ("RUNNING".equals(status)) {
            // fresh start or continuing after automated steps; nothing to pop
            assert true;
        }

        while (true) {
            if ("CANCELLED".equals(status) || "COMPLETED".equals(status)
                    || "FAILED".equals(status)) {
                return new AdvanceResult(status, current, actions);
            }
            Node node = model.node(current);
            switch (node.type()) {
                case "START" -> {
                    actions.add(new Action(ActionType.RECORD_STEP, current, null, 0, null,
                            "{\"started\":true}", null));
                    current = node.next();
                }
                case "APPROVAL" -> {
                    actions.add(new Action(ActionType.CREATE_TASK, current, node.assigneeRole(),
                            node.slaHours(), null, null, null));
                    return new AdvanceResult("WAITING_TASK", current, actions);
                }
                case "AUTOMATED" -> {
                    actions.add(new Action(ActionType.RECORD_STEP, current, null, 0, null,
                            "{\"automated\":true}", null));
                    if (node.compensationNode() != null && !node.compensationNode().isBlank()) {
                        actions.add(new Action(ActionType.RECORD_STEP, node.compensationNode(),
                                null, 0, null, "{\"compensatedFor\":\"" + current + "\"}", null));
                    }
                    current = node.next();
                }
                case "GATEWAY" -> {
                    String next = null;
                    for (WorkflowModel.Condition c : node.conditions()) {
                        if (c.isDefault()) {
                            next = c.next();
                            continue;
                        }
                        if (ExpressionEvaluator.evaluate(c.expression(), vars)) {
                            next = c.next();
                            break;
                        }
                    }
                    if (next == null) {
                        return new AdvanceResult("FAILED", current, actions);
                    }
                    actions.add(new Action(ActionType.RECORD_STEP, current, null, 0, null,
                            "{\"routedTo\":\"" + next + "\"}", null));
                    current = next;
                }
                case "TIMER" -> {
                    Instant resumeAt = now.plusSeconds(node.timerHours() * 3600L);
                    actions.add(new Action(ActionType.SET_TIMER, current, null, 0, resumeAt,
                            null, null));
                    return new AdvanceResult("WAITING_TIMER", current, actions);
                }
                case "END" -> {
                    actions.add(new Action(ActionType.COMPLETE, current, null, 0, null, null, null));
                    return new AdvanceResult("COMPLETED", current, actions);
                }
                default -> {
                    return new AdvanceResult("FAILED", current, actions);
                }
            }
        }
    }

    /** Nodes with declared compensation, in declaration order (callers reverse it). */
    public List<String> compensationNodes(WorkflowModel model) {
        List<String> out = new ArrayList<>();
        for (Node n : model.nodes()) {
            if (n.compensationNode() != null && !n.compensationNode().isBlank()
                    && !out.contains(n.compensationNode())) {
                out.add(n.compensationNode());
            }
        }
        return out;
    }

    private static String toJson(Map<String, Object> map) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
}
