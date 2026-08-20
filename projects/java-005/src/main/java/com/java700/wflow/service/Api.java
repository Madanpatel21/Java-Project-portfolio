package com.java700.wflow.service;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;

/** Request/response records for the workflow API. */
public final class Api {

    private Api() {
    }

    public record CreateDefinitionRequest(@NotBlank String definitionKey, @NotBlank String name,
                                          @NotBlank String definitionJson) {
    }

    public record StartRequest(@NotBlank String definitionKey, String businessKey,
                               Map<String, Object> variables) {

        public StartRequest {
            variables = variables == null ? Map.of() : Map.copyOf(variables);
        }
    }

    public record CompleteTaskRequest(Map<String, Object> result) {

        public CompleteTaskRequest {
            result = result == null ? Map.of() : Map.copyOf(result);
        }
    }

    public record DefinitionView(String id, String definitionKey, String name, int versionNo,
                                 String status, String createdBy, Instant createdAt) {
    }

    public record InstanceView(String id, String definitionId, String businessKey, String status,
                               String currentNodeId, Instant resumeAt, Instant startedAt,
                               Instant completedAt) {
    }

    public record TaskView(String id, String instanceId, String nodeId, String taskType,
                           String assigneeRole, String status, Map<String, Object> result,
                           Instant dueAt, Instant createdAt, Instant completedAt,
                           String completedBy) {

        public TaskView {
            result = result == null ? Map.of() : Map.copyOf(result);
        }
    }

    public record EscalationResult(int escalated) {
    }

    public record TimerResult(int resumed) {
    }
}
