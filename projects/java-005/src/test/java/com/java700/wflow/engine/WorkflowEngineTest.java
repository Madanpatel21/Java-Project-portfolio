package com.java700.wflow.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.java700.wflow.engine.WorkflowEngine.Action;
import com.java700.wflow.engine.WorkflowEngine.ActionType;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkflowEngineTest {

    private static final Instant NOW = Instant.parse("2026-08-20T00:00:00Z");
    private static final String DEF = """
            {"nodes":[
              {"id":"start","type":"START","next":"review"},
              {"id":"review","type":"APPROVAL","role":"APPROVER","slaHours":24,"next":"gw1","compensation":"comp1"},
              {"id":"gw1","type":"GATEWAY","conditions":[
                 {"expr":"var.amount > 1000","next":"legal"},
                 {"default":true,"next":"timer1"}]},
              {"id":"legal","type":"APPROVAL","role":"LEGAL","slaHours":48,"next":"end"},
              {"id":"timer1","type":"TIMER","timerHours":24,"next":"end"},
              {"id":"comp1","type":"APPROVAL","role":"COMPENSATION","slaHours":24,"next":"end"},
              {"id":"end","type":"END"}
            ]}""";

    private WorkflowEngine engine;
    private WorkflowModel model;

    @BeforeEach
    void setUp() {
        engine = new WorkflowEngine();
        model = WorkflowModel.parse(DEF);
    }

    @Test
    void startCreatesFirstApprovalTask() {
        WorkflowEngine.AdvanceResult result = engine.advance(model, "RUNNING", "start",
                new HashMap<>(), null, NOW);
        assertThat(result.status()).isEqualTo("WAITING_TASK");
        assertThat(result.currentNodeId()).isEqualTo("review");
        assertThat(result.actions()).extracting(Action::type)
                .contains(ActionType.CREATE_TASK, ActionType.RECORD_STEP);
    }

    @Test
    void taskCompletionRoutesThroughGatewayHighAmount() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("amount", 5000);
        Map<String, Object> taskResult = Map.of("approved", true);
        WorkflowEngine.AdvanceResult result = engine.advance(model, "WAITING_TASK", "review",
                vars, taskResult, NOW);
        assertThat(result.status()).isEqualTo("WAITING_TASK");
        assertThat(result.currentNodeId()).isEqualTo("legal");
    }

    @Test
    void taskCompletionRoutesToTimerOnDefaultBranch() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("amount", 50);
        WorkflowEngine.AdvanceResult result = engine.advance(model, "WAITING_TASK", "review",
                vars, Map.of("approved", true), NOW);
        assertThat(result.status()).isEqualTo("WAITING_TIMER");
        assertThat(result.currentNodeId()).isEqualTo("timer1");
        assertThat(result.actions()).anyMatch(a -> a.type() == ActionType.SET_TIMER
                && a.resumeAt() != null);
    }

    @Test
    void timerFireResumesToEndAndCompletes() {
        WorkflowEngine.AdvanceResult result = engine.advance(model, "WAITING_TIMER", "timer1",
                new HashMap<>(), null, NOW);
        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.actions()).extracting(Action::type).contains(ActionType.COMPLETE);
    }

    @Test
    void taskResultVariablesMergeIntoInstance() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("amount", 10);
        Map<String, Object> taskResult = new HashMap<>();
        taskResult.put("approved", true);
        Map<String, Object> merged = new HashMap<>();
        merged.put("amount", 10);
        merged.put("extra", "yes");
        taskResult.put("vars", Map.of("extra", "yes"));
        WorkflowEngine.AdvanceResult result = engine.advance(model, "WAITING_TASK", "review",
                vars, taskResult, NOW);
        boolean updated = result.actions().stream()
                .anyMatch(a -> a.type() == ActionType.UPDATE_VARS
                        && "yes".equals(String.valueOf(a.vars().get("extra"))));
        assertThat(updated).isTrue();
    }

    @Test
    void compensationNodesDeclared() {
        List<String> comps = engine.compensationNodes(model);
        assertThat(comps).containsExactly("comp1");
    }

    @Test
    void malformedDefinitionRejected() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                WorkflowModel.parse("{\"nodes\":[{\"id\":\"x\",\"type\":\"END\"}]}"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gatewayFailsWhenNoConditionMatches() {
        String badDef = """
                {"nodes":[
                  {"id":"start","type":"START","next":"gw1"},
                  {"id":"gw1","type":"GATEWAY","conditions":[{"expr":"var.x == 1","next":"end"}]},
                  {"id":"end","type":"END"}
                ]}""";
        WorkflowModel bad = WorkflowModel.parse(badDef);
        WorkflowEngine.AdvanceResult result = engine.advance(bad, "RUNNING", "start",
                new HashMap<>(), null, NOW);
        assertThat(result.status()).isEqualTo("FAILED");
    }
}
