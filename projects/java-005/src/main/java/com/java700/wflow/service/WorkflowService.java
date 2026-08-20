package com.java700.wflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.wflow.common.api.PageResponse;
import com.java700.wflow.common.api.Problems;
import com.java700.wflow.common.audit.AuditLogService;
import com.java700.wflow.common.web.IdempotencyService;
import com.java700.wflow.domain.WorkflowDefinition;
import com.java700.wflow.domain.WorkflowDefinitionRepository;
import com.java700.wflow.domain.WorkflowInstance;
import com.java700.wflow.domain.WorkflowInstanceRepository;
import com.java700.wflow.domain.WorkflowStep;
import com.java700.wflow.domain.WorkflowStepRepository;
import com.java700.wflow.domain.WorkflowTask;
import com.java700.wflow.domain.WorkflowTaskRepository;
import com.java700.wflow.engine.WorkflowEngine;
import com.java700.wflow.engine.WorkflowEngine.Action;
import com.java700.wflow.engine.WorkflowModel;
import com.java700.wflow.messaging.DomainEvent;
import com.java700.wflow.messaging.DomainEventBus;
import com.java700.wflow.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Workflow orchestration: versioned definitions, instances pinned to their version,
 * engine-driven advancement, human tasks with SLA escalation, timers and cancellation
 * with compensation.
 */
@Service
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowTaskRepository taskRepository;
    private final WorkflowStepRepository stepRepository;
    private final WorkflowEngine engine;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final ObjectMapper mapper;
    private final Clock clock;

    public WorkflowService(WorkflowDefinitionRepository definitionRepository,
                           WorkflowInstanceRepository instanceRepository,
                           WorkflowTaskRepository taskRepository,
                           WorkflowStepRepository stepRepository,
                           WorkflowEngine engine, DomainEventBus bus,
                           IdempotencyService idempotency, AuditLogService audit,
                           ObjectMapper mapper, Clock clock) {
        this.definitionRepository = definitionRepository;
        this.instanceRepository = instanceRepository;
        this.taskRepository = taskRepository;
        this.stepRepository = stepRepository;
        this.engine = engine;
        this.bus = bus;
        this.idempotency = idempotency;
        this.audit = audit;
        this.mapper = mapper;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- definitions

    @Transactional
    public Api.DefinitionView createDefinition(Api.CreateDefinitionRequest req, String idemKey) {
        String existing = idempotency.begin(idemKey, "WORKFLOW_DEFINITION");
        if (existing != null) {
            return view(loadDefinition(existing));
        }
        try {
            try {
                WorkflowModel.parse(req.definitionJson()); // validate before persisting
            } catch (IllegalArgumentException e) {
                throw new Problems.BadRequest(e.getMessage());
            }
            int next = definitionRepository.findByDefinitionKeyOrderByVersionNoDesc(
                    req.definitionKey()).stream().findFirst()
                    .map(WorkflowDefinition::getVersionNo).orElse(0) + 1;
            definitionRepository.findByDefinitionKeyAndStatus(req.definitionKey(), "ACTIVE")
                    .ifPresent(active -> {
                        active.deprecate();
                        definitionRepository.save(active);
                    });
            WorkflowDefinition definition = new WorkflowDefinition(UUID.randomUUID().toString(),
                    req.definitionKey(), req.name(), next, req.definitionJson(),
                    SecurityUtil.currentUsername(), Instant.now(clock));
            definitionRepository.save(definition);
            audit.record("WORKFLOW_DEFINITION_CREATED", "WORKFLOW_DEFINITION", definition.getId(),
                    req.definitionKey() + " v" + next);
            idempotency.complete(idemKey, definition.getId(), 201);
            return view(definition);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    // ---------------------------------------------------------------- instances

    @Transactional
    public Api.InstanceView start(Api.StartRequest req, String idemKey) {
        String existing = idempotency.begin(idemKey, "WORKFLOW_START");
        if (existing != null) {
            return instanceView(loadInstance(existing));
        }
        try {
            WorkflowDefinition definition = definitionRepository
                    .findByDefinitionKeyAndStatus(req.definitionKey(), "ACTIVE")
                    .orElseThrow(() -> new Problems.NotFound("No active definition for key "
                            + req.definitionKey()));
            String businessKey = req.businessKey() == null || req.businessKey().isBlank()
                    ? UUID.randomUUID().toString() : req.businessKey();
            instanceRepository.findByDefinitionIdAndBusinessKey(definition.getId(), businessKey)
                    .ifPresent(i -> {
                        throw new Problems.Conflict("An instance for this business key already exists");
                    });
            Map<String, Object> vars = req.variables() == null
                    ? new HashMap<>() : new HashMap<>(req.variables());
            WorkflowInstance instance = new WorkflowInstance(UUID.randomUUID().toString(),
                    definition.getId(), businessKey, toJson(vars), Instant.now(clock));
            instance.setState(WorkflowInstance.Status.RUNNING,
                    WorkflowModel.parse(definition.getDefinitionJson()).startId());
            instanceRepository.save(instance);
            audit.record("WORKFLOW_STARTED", "WORKFLOW_INSTANCE", instance.getId(),
                    req.definitionKey() + " businessKey=" + businessKey);
            advance(instance, definition, vars, null);
            idempotency.complete(idemKey, instance.getId(), 201);
            return instanceView(instance);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    @Transactional
    public Api.InstanceView completeTask(String taskId, Map<String, Object> result,
                                         String idemKey) {
        String existing = idempotency.begin(idemKey, "TASK_COMPLETE");
        WorkflowTask task;
        try {
            task = loadTask(taskId);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
        if (existing != null) {
            return instanceView(loadInstance(task.getInstanceId()));
        }
        if (task.getStatus() != WorkflowTask.Status.PENDING) {
            throw new Problems.Conflict("Task is not pending");
        }
        task.complete(toJson(result == null ? Map.of() : result),
                SecurityUtil.currentUsername(), Instant.now(clock));
        taskRepository.save(task);
        WorkflowInstance instance = loadInstance(task.getInstanceId());
        WorkflowDefinition definition = loadDefinition(instance.getDefinitionId());
        Map<String, Object> vars = parse(instance.getVariablesJson());
        advance(instance, definition, vars, result);
        audit.record("TASK_COMPLETED", "WORKFLOW_TASK", taskId,
                "By " + SecurityUtil.currentUsername());
        idempotency.complete(idemKey, taskId, 200);
        return instanceView(instance);
    }

    @Transactional
    public Api.InstanceView cancel(String instanceId) {
        WorkflowInstance instance = loadInstance(instanceId);
        if (instance.getStatus() == WorkflowInstance.Status.COMPLETED
                || instance.getStatus() == WorkflowInstance.Status.CANCELLED) {
            throw new Problems.Conflict("Instance is already finished");
        }
        // 1. skip the instance's original pending tasks first
        for (WorkflowTask t : taskRepository.findByInstanceIdAndStatus(instanceId, "PENDING")) {
            t.skip(Instant.now(clock));
            taskRepository.save(t);
        }
        // 2. compensation: reverse-declared compensation nodes of executed steps
        WorkflowDefinition definition = loadDefinition(instance.getDefinitionId());
        WorkflowModel model = WorkflowModel.parse(definition.getDefinitionJson());
        List<String> compensations = engine.compensationNodes(model);
        var reversed = compensations.reversed();
        for (String nodeId : reversed) {
            WorkflowModel.Node node = model.node(nodeId);
            taskRepository.save(new WorkflowTask(UUID.randomUUID().toString(), instanceId,
                    nodeId, WorkflowTask.Type.COMPENSATION, node.assigneeRole(),
                    Instant.now(clock).plusSeconds(node.slaHours() * 3600L), Instant.now(clock)));
        }
        instance.finish(WorkflowInstance.Status.CANCELLED, Instant.now(clock));
        instanceRepository.save(instance);
        audit.record("WORKFLOW_CANCELLED", "WORKFLOW_INSTANCE", instanceId,
                "Compensation tasks: " + reversed.size());
        bus.publish(new WorkflowCancelled(UUID.randomUUID().toString(), Instant.now(clock),
                instanceId, instance.getBusinessKey()));
        return instanceView(instance);
    }

    /** Applies the engine plan to the instance transactionally. */
    private void advance(WorkflowInstance instance, WorkflowDefinition definition,
                         Map<String, Object> vars, Map<String, Object> taskResult) {
        WorkflowModel model = WorkflowModel.parse(definition.getDefinitionJson());
        WorkflowEngine.AdvanceResult result = engine.advance(model,
                instance.getStatus().name(), instance.getCurrentNodeId(), vars, taskResult,
                Instant.now(clock));
        for (Action action : result.actions()) {
            switch (action.type()) {
                case CREATE_TASK -> {
                    WorkflowTask task = new WorkflowTask(UUID.randomUUID().toString(),
                            instance.getId(), action.nodeId(), WorkflowTask.Type.APPROVAL,
                            action.assigneeRole(),
                            Instant.now(clock).plusSeconds(action.slaHours() * 3600L),
                            Instant.now(clock));
                    taskRepository.save(task);
                    bus.publish(new TaskCreated(UUID.randomUUID().toString(),
                            Instant.now(clock), task.getId(), instance.getId(),
                            task.getAssigneeRole()));
                }
                case SET_TIMER -> {
                    instance.setResumeAt(action.resumeAt());
                }
                case RECORD_STEP -> stepRepository.save(new WorkflowStep(
                        UUID.randomUUID().toString(), instance.getId(), action.nodeId(),
                        "STEP", action.resultJson(), Instant.now(clock)));
                case UPDATE_VARS -> {
                    instance.updateVariables(toJson(action.vars()));
                }
                case COMPLETE -> {
                    instance.finish(WorkflowInstance.Status.COMPLETED, Instant.now(clock));
                    bus.publish(new WorkflowCompleted(UUID.randomUUID().toString(),
                            Instant.now(clock), instance.getId(), instance.getBusinessKey()));
                }
                default -> {
                    throw new Problems.BadRequest("Unknown engine action: " + action.type());
                }
            }
        }
        instance.setState(WorkflowInstance.Status.valueOf(result.status()),
                result.currentNodeId());
        instanceRepository.save(instance);
    }

    // ---------------------------------------------------------------- schedulers

    /** Resume instances whose timers fired. */
    @Transactional
    public Api.TimerResult resumeTimers() {
        int resumed = 0;
        for (WorkflowInstance instance : instanceRepository.findByStatusAndResumeAtBefore(
                "WAITING_TIMER", Instant.now(clock))) {
            WorkflowDefinition definition = loadDefinition(instance.getDefinitionId());
            advance(instance, definition, parse(instance.getVariablesJson()), null);
            resumed++;
        }
        if (resumed > 0) {
            log.info("Timer scan: {} instances resumed", resumed);
        }
        return new Api.TimerResult(resumed);
    }

    /** Escalate overdue tasks: extend the SLA and flag via events. */
    @Transactional
    public Api.EscalationResult escalateOverdue() {
        int escalated = 0;
        for (WorkflowTask task : taskRepository.findByStatusAndDueAtBefore(
                "PENDING", Instant.now(clock))) {
            task.escalate(Instant.now(clock).plusSeconds(24 * 3600L));
            taskRepository.save(task);
            bus.publish(new TaskEscalated(UUID.randomUUID().toString(), Instant.now(clock),
                    task.getId(), task.getInstanceId(), task.getAssigneeRole()));
            escalated++;
        }
        if (escalated > 0) {
            log.info("Escalation scan: {} tasks escalated", escalated);
        }
        return new Api.EscalationResult(escalated);
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public PageResponse<Api.TaskView> worklist(String role, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100), Sort.by("dueAt"));
        var result = role == null
                ? taskRepository.findByStatus("PENDING", pr)
                : taskRepository.findByStatusAndAssigneeRole("PENDING", role, pr);
        return PageResponse.from(result.map(this::taskView));
    }

    @Transactional(readOnly = true)
    public Api.InstanceView instance(String instanceId) {
        return instanceView(loadInstance(instanceId));
    }

    @Transactional(readOnly = true)
    public List<Api.DefinitionView> definitions(String key) {
        return (key == null
                ? definitionRepository.findAll()
                : definitionRepository.findByDefinitionKeyOrderByVersionNoDesc(key))
                .stream().map(this::view).toList();
    }

    // ---------------------------------------------------------------- helpers

    private WorkflowDefinition loadDefinition(String id) {
        return definitionRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Definition not found"));
    }

    private WorkflowInstance loadInstance(String id) {
        return instanceRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Instance not found"));
    }

    private WorkflowTask loadTask(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Task not found"));
    }

    private Api.DefinitionView view(WorkflowDefinition d) {
        return new Api.DefinitionView(d.getId(), d.getDefinitionKey(), d.getName(),
                d.getVersionNo(), d.getStatus().name(), d.getCreatedBy(), d.getCreatedAt());
    }

    private Api.InstanceView instanceView(WorkflowInstance i) {
        return new Api.InstanceView(i.getId(), i.getDefinitionId(), i.getBusinessKey(),
                i.getStatus().name(), i.getCurrentNodeId(), i.getResumeAt(), i.getStartedAt(),
                i.getCompletedAt());
    }

    private Api.TaskView taskView(WorkflowTask t) {
        return new Api.TaskView(t.getId(), t.getInstanceId(), t.getNodeId(),
                t.getType().name(), t.getAssigneeRole(), t.getStatus().name(),
                parse(t.getResultJson()), t.getDueAt(), t.getCreatedAt(),
                t.getCompletedAt(), t.getCompletedBy());
    }

    private String toJson(Map<String, Object> map) {
        try {
            return mapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new Problems.BadRequest("Invalid variables payload");
        }
    }

    private Map<String, Object> parse(String json) {
        if (json == null) {
            return new HashMap<>();
        }
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public record WorkflowCompleted(String eventId, Instant occurredAt, String instanceId,
                                    String businessKey) implements DomainEvent {
    }

    public record WorkflowCancelled(String eventId, Instant occurredAt, String instanceId,
                                    String businessKey) implements DomainEvent {
    }

    public record TaskCreated(String eventId, Instant occurredAt, String taskId,
                              String instanceId, String assigneeRole) implements DomainEvent {
    }

    public record TaskEscalated(String eventId, Instant occurredAt, String taskId,
                                String instanceId, String assigneeRole) implements DomainEvent {
    }
}
