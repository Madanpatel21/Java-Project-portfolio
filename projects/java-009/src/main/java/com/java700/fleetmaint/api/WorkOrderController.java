package com.java700.fleetmaint.api;

import com.java700.fleetmaint.domain.WorkOrder;
import com.java700.fleetmaint.security.SecurityUtil;
import com.java700.fleetmaint.service.Api;
import com.java700.fleetmaint.service.WorkOrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Work order lifecycle: open from a due task, start, complete, reject, retry parts. */
@RestController
@RequestMapping("/api/v1/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrders;

    public WorkOrderController(WorkOrderService workOrders) {
        this.workOrders = workOrders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','ADMIN')")
    public Api.WorkOrderView open(@RequestBody OpenRequest request) {
        return workOrders.view(workOrders.open(request.taskId(),
                SecurityUtil.currentUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','MECHANIC','PARTS_CLERK','AUDITOR','ADMIN')")
    public List<Api.WorkOrderView> list(@RequestParam(required = false) String status) {
        List<WorkOrder> orders = status == null ? workOrders.byStatus(WorkOrder.STATUS_OPEN)
                : workOrders.byStatus(status);
        if (status == null) {
            orders = workOrders.byStatus(WorkOrder.STATUS_OPEN);
            orders.addAll(workOrders.byStatus(WorkOrder.STATUS_PARTS_HOLD));
            orders.addAll(workOrders.byStatus(WorkOrder.STATUS_IN_PROGRESS));
        }
        return WorkOrderService.sortOldestFirst(orders).stream().map(workOrders::view).toList();
    }

    @GetMapping("/{workOrderId}")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','MECHANIC','PARTS_CLERK','AUDITOR','ADMIN')")
    public Api.WorkOrderView get(@PathVariable String workOrderId) {
        return workOrders.view(workOrders.load(workOrderId));
    }

    @PostMapping("/{workOrderId}/start")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','MECHANIC','ADMIN')")
    public Api.WorkOrderView start(@PathVariable String workOrderId,
                                   @Valid @RequestBody Api.WorkOrderStartRequest request) {
        return workOrders.view(workOrders.start(workOrderId, request.mechanic(),
                SecurityUtil.currentUsername()));
    }

    @PostMapping("/{workOrderId}/complete")
    @PreAuthorize("hasAnyRole('MECHANIC','FLEET_MANAGER','ADMIN')")
    public Api.WorkOrderView complete(@PathVariable String workOrderId,
                                      @Valid @RequestBody Api.WorkOrderCompleteRequest request) {
        return workOrders.view(workOrders.complete(workOrderId, request,
                SecurityUtil.currentUsername()));
    }

    @PostMapping("/{workOrderId}/reject")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','ADMIN')")
    public Api.WorkOrderView reject(@PathVariable String workOrderId,
                                    @RequestBody(required = false) NoteRequest request) {
        String note = request == null ? null : request.note();
        return workOrders.view(workOrders.reject(workOrderId, note,
                SecurityUtil.currentUsername()));
    }

    @PostMapping("/{workOrderId}/retry-parts")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','PARTS_CLERK','ADMIN')")
    public Api.WorkOrderView retryParts(@PathVariable String workOrderId) {
        return workOrders.view(workOrders.retryParts(workOrderId,
                SecurityUtil.currentUsername()));
    }

    /** Open-by-task payload. */
    public record OpenRequest(String taskId) {
    }

    /** Simple note payload. */
    public record NoteRequest(String note) {
    }
}
