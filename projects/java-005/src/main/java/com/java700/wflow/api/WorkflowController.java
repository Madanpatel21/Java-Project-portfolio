package com.java700.wflow.api;

import com.java700.wflow.common.api.PageResponse;
import com.java700.wflow.service.Api;
import com.java700.wflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "workflow", description = "Versioned model-driven workflow orchestration")
public class WorkflowController {

    private final WorkflowService service;

    public WorkflowController(WorkflowService service) {
        this.service = service;
    }

    @PostMapping("/definitions")
    @Operation(summary = "Create a workflow definition (validated model; new version deprecates the previous)")
    @PreAuthorize("hasAnyRole('PROCESS_ADMIN','ADMIN')")
    public Api.DefinitionView createDefinition(@Valid @RequestBody Api.CreateDefinitionRequest body,
                                               @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.createDefinition(body, idemKey);
    }

    @GetMapping("/definitions")
    @Operation(summary = "List definitions (optionally filtered by key)")
    @PreAuthorize("isAuthenticated()")
    public List<Api.DefinitionView> definitions(@RequestParam(required = false) String key) {
        return service.definitions(key);
    }

    @PostMapping("/instances")
    @Operation(summary = "Start a workflow instance (idempotent per business key)")
    @PreAuthorize("hasAnyRole('PROCESS_OPERATOR','PROCESS_ADMIN','ADMIN')")
    public Api.InstanceView start(@Valid @RequestBody Api.StartRequest body,
                                  @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.start(body, idemKey);
    }

    @GetMapping("/instances/{id}")
    @Operation(summary = "Get an instance's state")
    @PreAuthorize("isAuthenticated()")
    public Api.InstanceView instance(@PathVariable String id) {
        return service.instance(id);
    }

    @PostMapping("/tasks/{id}/complete")
    @Operation(summary = "Complete a task (result may carry variable updates and routing decisions)")
    @PreAuthorize("hasAnyRole('APPROVER','PROCESS_ADMIN','ADMIN')")
    public Api.InstanceView completeTask(@PathVariable String id,
                                         @RequestHeader(value = "Idempotency-Key") String idemKey,
                                         @Valid @RequestBody(required = false) Api.CompleteTaskRequest body) {
        return service.completeTask(id, body == null ? null : body.result(), idemKey);
    }

    @PostMapping("/instances/{id}/cancel")
    @Operation(summary = "Cancel an instance (compensation tasks are created)")
    @PreAuthorize("hasAnyRole('PROCESS_OPERATOR','PROCESS_ADMIN','ADMIN')")
    public Api.InstanceView cancel(@PathVariable String id) {
        return service.cancel(id);
    }

    @GetMapping("/tasks")
    @Operation(summary = "Worklist for a role (pending tasks)")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<Api.TaskView> worklist(@RequestParam(required = false) String role,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return service.worklist(role, page, size);
    }

    @PostMapping("/scheduler/timers")
    @Operation(summary = "Resume instances whose timers fired")
    @PreAuthorize("hasAnyRole('PROCESS_ADMIN','ADMIN')")
    public Api.TimerResult resumeTimers() {
        return service.resumeTimers();
    }

    @PostMapping("/scheduler/escalations")
    @Operation(summary = "Escalate tasks past their SLA")
    @PreAuthorize("hasAnyRole('PROCESS_ADMIN','ADMIN')")
    public Api.EscalationResult escalate() {
        return service.escalateOverdue();
    }
}
