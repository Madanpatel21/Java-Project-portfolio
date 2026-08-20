package com.java700.fleetmaint.service;

import com.java700.fleetmaint.common.api.Problems;
import com.java700.fleetmaint.common.audit.AuditLogService;
import com.java700.fleetmaint.domain.MaintenanceTask;
import com.java700.fleetmaint.domain.MaintenanceTaskRepository;
import com.java700.fleetmaint.domain.Part;
import com.java700.fleetmaint.domain.PartRepository;
import com.java700.fleetmaint.domain.PartReservation;
import com.java700.fleetmaint.domain.PartReservationRepository;
import com.java700.fleetmaint.domain.PlanItem;
import com.java700.fleetmaint.domain.PlanItemRepository;
import com.java700.fleetmaint.domain.Vehicle;
import com.java700.fleetmaint.domain.VehicleRepository;
import com.java700.fleetmaint.domain.WorkOrder;
import com.java700.fleetmaint.domain.WorkOrderRepository;
import com.java700.fleetmaint.messaging.DomainEventBus;
import com.java700.fleetmaint.messaging.FleetEvents;
import com.java700.fleetmaint.observability.Metrics;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Work order lifecycle with parts kitting and reservations.
 *
 * <p>OPEN -&gt; IN_PROGRESS -&gt; COMPLETED, with PARTS_HOLD when the kit cannot be fully
 * reserved from inventory. Completion issues reserved parts, debits stock, writes the
 * vehicle odometer and closes the task. Rejection releases reservations.</p>
 */
@Service
public class WorkOrderService {

    private static final List<String> OPENABLE_TASKS = List.of(
            MaintenanceTask.STATUS_DUE, MaintenanceTask.STATUS_OVERDUE,
            MaintenanceTask.STATUS_SCHEDULED);

    private final WorkOrderRepository workOrders;
    private final MaintenanceTaskRepository tasks;
    private final VehicleRepository vehicles;
    private final PlanItemRepository planItems;
    private final PartRepository parts;
    private final PartReservationRepository reservations;
    private final DomainEventBus events;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;

    public WorkOrderService(WorkOrderRepository workOrders, MaintenanceTaskRepository tasks,
                            VehicleRepository vehicles, PlanItemRepository planItems,
                            PartRepository parts, PartReservationRepository reservations,
                            DomainEventBus events, AuditLogService audit, Metrics metrics,
                            Clock clock) {
        this.workOrders = workOrders;
        this.tasks = tasks;
        this.vehicles = vehicles;
        this.planItems = planItems;
        this.parts = parts;
        this.reservations = reservations;
        this.events = events;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
    }

    /** Opens a work order for a due task and reserves the plan's parts kit. */
    @Transactional
    public WorkOrder open(String taskId, String username) {
        MaintenanceTask task = tasks.findById(taskId)
                .orElseThrow(() -> new Problems.NotFound("maintenance task " + taskId));
        if (!OPENABLE_TASKS.contains(task.getStatus())) {
            throw new Problems.Conflict("task " + task.getTaskNo() + " is " + task.getStatus());
        }
        if (task.getWorkOrderId() != null) {
            return workOrders.findById(task.getWorkOrderId())
                    .orElseThrow(() -> new Problems.NotFound("work order " + task.getWorkOrderId()));
        }
        Vehicle vehicle = vehicles.findById(task.getVehicleId())
                .orElseThrow(() -> new Problems.NotFound("vehicle of task " + task.getTaskNo()));

        WorkOrder workOrder = new WorkOrder(UUID.randomUUID().toString(), nextWoNo(), taskId,
                vehicle.getId(), WorkOrder.STATUS_OPEN, username, null, null, null, null, null,
                null, null, Instant.now(clock), null);
        WorkOrder saved = workOrders.save(workOrder);

        String shortfall = reserveKit(saved.getId(), task.getPlanId());
        if (shortfall != null) {
            saved.holdForParts(shortfall);
            metrics.workOrderOnPartsHold();
            audit.record("WO_PARTS_HOLD", "work_order", saved.getWoNo(), shortfall);
        }
        task.assignWorkOrder(saved.getId());
        tasks.save(task);
        vehicle.transition(Vehicle.STATUS_IN_SHOP);
        vehicles.save(vehicle);
        audit.record("WO_OPENED", "work_order", saved.getWoNo(),
                "task=" + task.getTaskNo() + " by=" + username);
        return workOrders.save(saved);
    }

    /** Starts the work (OPEN -&gt; IN_PROGRESS). A parts hold must be resolved first. */
    @Transactional
    public WorkOrder start(String workOrderId, String mechanic, String username) {
        WorkOrder workOrder = load(workOrderId);
        if (WorkOrder.STATUS_PARTS_HOLD.equals(workOrder.getStatus())) {
            throw new Problems.Conflict("work order " + workOrder.getWoNo()
                    + " is on PARTS_HOLD — resolve the parts shortfall first");
        }
        if (!WorkOrder.STATUS_OPEN.equals(workOrder.getStatus())) {
            throw new Problems.Conflict("work order " + workOrder.getWoNo() + " is "
                    + workOrder.getStatus());
        }
        workOrder.transition(WorkOrder.STATUS_IN_PROGRESS);
        audit.record("WO_STARTED", "work_order", workOrder.getWoNo(),
                "mechanic=" + mechanic + " by=" + username);
        return workOrders.save(workOrder);
    }

    /** Completes the work: issues parts, records costs, updates vehicle and task. */
    @Transactional
    public WorkOrder complete(String workOrderId, Api.WorkOrderCompleteRequest request,
                              String username) {
        WorkOrder workOrder = load(workOrderId);
        if (!WorkOrder.STATUS_IN_PROGRESS.equals(workOrder.getStatus())) {
            throw new Problems.Conflict("work order " + workOrder.getWoNo() + " is "
                    + workOrder.getStatus());
        }
        BigDecimal laborCost = request.laborCost();
        if (laborCost == null || laborCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new Problems.BadRequest("laborCost must be >= 0");
        }
        List<PartReservation> active = reservations.findByWorkOrderIdAndStatus(workOrderId,
                PartReservation.STATUS_RESERVED);
        BigDecimal partsCost = BigDecimal.ZERO;
        for (PartReservation reservation : active) {
            Part part = parts.findByPartCode(reservation.getPartCode())
                    .orElseThrow(() -> new Problems.NotFound(
                            "part " + reservation.getPartCode()));
            part.issue(reservation.getQuantity());
            parts.save(part);
            reservation.transition(PartReservation.STATUS_ISSUED);
            reservations.save(reservation);
            partsCost = partsCost.add(part.getUnitCost()
                    .multiply(BigDecimal.valueOf(reservation.getQuantity())));
            metrics.partsIssued(reservation.getQuantity());
        }
        workOrder.complete(request.mechanic(), request.laborHours(), laborCost, partsCost,
                request.odometerAtService(), Instant.now(clock));
        WorkOrder saved = workOrders.save(workOrder);

        MaintenanceTask task = tasks.findById(workOrder.getTaskId())
                .orElseThrow(() -> new Problems.NotFound("task " + workOrder.getTaskId()));
        task.complete(Instant.now(clock));
        tasks.save(task);

        Vehicle vehicle = vehicles.findById(workOrder.getVehicleId())
                .orElseThrow(() -> new Problems.NotFound("vehicle " + workOrder.getVehicleId()));
        if (request.odometerAtService() != null
                && request.odometerAtService() > vehicle.getCurrentOdometer()) {
            vehicle.applyOdometer(request.odometerAtService(), Instant.now(clock));
        }
        if (!Vehicle.STATUS_COMPLIANCE_HOLD.equals(vehicle.getStatus())) {
            vehicle.transition(Vehicle.STATUS_ACTIVE);
        }
        vehicles.save(vehicle);

        metrics.workOrderCompleted();
        audit.record("WO_COMPLETED", "work_order", saved.getWoNo(),
                "task=" + task.getTaskNo() + " parts=" + partsCost.toPlainString()
                        + " labor=" + laborCost.toPlainString() + " by=" + username);
        events.publish(new FleetEvents.WorkOrderCompleted(UUID.randomUUID().toString(),
                Instant.now(clock), saved.getId(), saved.getWoNo(), vehicle.getId(),
                vehicle.getPlate()));
        return saved;
    }

    /** Rejects a work order and releases any part reservations. */
    @Transactional
    public WorkOrder reject(String workOrderId, String note, String username) {
        WorkOrder workOrder = load(workOrderId);
        if (WorkOrder.STATUS_COMPLETED.equals(workOrder.getStatus())) {
            throw new Problems.Conflict("completed work order cannot be rejected");
        }
        for (PartReservation reservation : reservations.findByWorkOrderIdAndStatus(workOrderId,
                PartReservation.STATUS_RESERVED)) {
            Part part = parts.findByPartCode(reservation.getPartCode()).orElse(null);
            if (part != null) {
                part.release(reservation.getQuantity());
                parts.save(part);
            }
            reservation.transition(PartReservation.STATUS_CANCELLED);
            reservations.save(reservation);
        }
        workOrder.transition(WorkOrder.STATUS_REJECTED);
        MaintenanceTask task = tasks.findById(workOrder.getTaskId()).orElse(null);
        if (task != null) {
            task.markDue(MaintenanceTask.STATUS_DUE);
            tasks.save(task);
        }
        audit.record("WO_REJECTED", "work_order", workOrder.getWoNo(),
                "note=" + (note == null ? "" : note) + " by=" + username);
        return workOrders.save(workOrder);
    }

    /** Retries the parts kit reservation after a restock (PARTS_HOLD -&gt; OPEN). */
    @Transactional
    public WorkOrder retryParts(String workOrderId, String username) {
        WorkOrder workOrder = load(workOrderId);
        if (!WorkOrder.STATUS_PARTS_HOLD.equals(workOrder.getStatus())) {
            throw new Problems.Conflict("work order " + workOrder.getWoNo() + " is "
                    + workOrder.getStatus());
        }
        MaintenanceTask task = tasks.findById(workOrder.getTaskId())
                .orElseThrow(() -> new Problems.NotFound("task " + workOrder.getTaskId()));
        List<PartReservation> existing = reservations.findByWorkOrderId(workOrderId);
        for (PartReservation reservation : existing) {
            if (PartReservation.STATUS_RESERVED.equals(reservation.getStatus())) {
                continue;
            }
            reservations.delete(reservation);
        }
        String shortfall = reserveKit(workOrderId, task.getPlanId());
        if (shortfall != null) {
            workOrder.holdForParts(shortfall);
            return workOrders.save(workOrder);
        }
        workOrder.transition(WorkOrder.STATUS_OPEN);
        audit.record("WO_PARTS_RESOLVED", "work_order", workOrder.getWoNo(), "by=" + username);
        return workOrders.save(workOrder);
    }

    @Transactional(readOnly = true)
    public WorkOrder load(String workOrderId) {
        return workOrders.findById(workOrderId)
                .orElseThrow(() -> new Problems.NotFound("work order " + workOrderId));
    }

    @Transactional(readOnly = true)
    public List<WorkOrder> byStatus(String status) {
        return workOrders.findByStatusOrderByOpenedAtAsc(status);
    }

    @Transactional(readOnly = true)
    public Api.WorkOrderView view(WorkOrder workOrder) {
        MaintenanceTask task = tasks.findById(workOrder.getTaskId()).orElse(null);
        Vehicle vehicle = vehicles.findById(workOrder.getVehicleId()).orElse(null);
        List<Api.ReservationView> reservationViews = new ArrayList<>();
        for (PartReservation reservation : reservations.findByWorkOrderId(workOrder.getId())) {
            Part part = parts.findByPartCode(reservation.getPartCode()).orElse(null);
            reservationViews.add(new Api.ReservationView(reservation.getReservationNo(),
                    reservation.getPartCode(), part == null ? "?" : part.getName(),
                    reservation.getQuantity(), reservation.getStatus()));
        }
        BigDecimal totalCost = BigDecimal.ZERO;
        if (workOrder.getLaborCost() != null) {
            totalCost = totalCost.add(workOrder.getLaborCost());
        }
        if (workOrder.getPartsCost() != null) {
            totalCost = totalCost.add(workOrder.getPartsCost());
        }
        return new Api.WorkOrderView(workOrder.getId(), workOrder.getWoNo(), workOrder.getTaskId(),
                task == null ? "?" : task.getTaskNo(), workOrder.getVehicleId(),
                vehicle == null ? "?" : vehicle.getPlate(), workOrder.getStatus(),
                workOrder.getOpenedBy(), workOrder.getMechanic(), workOrder.getLaborHours(),
                workOrder.getLaborCost(), workOrder.getPartsCost(), totalCost, workOrder.getNotes(),
                workOrder.getShortfallReason(), workOrder.getOdometerAtService(),
                workOrder.getOpenedAt(), workOrder.getCompletedAt(), reservationViews);
    }

    /** Reserves the plan kit; returns a shortfall description or null when fully reserved. */
    private String reserveKit(String workOrderId, String planId) {
        List<PlanItem> items = planItems.findByPlanId(planId);
        List<String> shortages = new ArrayList<>();
        for (PlanItem item : items) {
            Part part = parts.findByPartCode(item.getPartCode()).orElse(null);
            if (part == null) {
                shortages.add(item.getPartName() + " (unknown part)");
                continue;
            }
            int available = part.getQuantityOnHand() - part.getReservedQty();
            if (available < item.getQuantity()) {
                shortages.add(item.getPartName() + " (" + item.getQuantity()
                        + " needed, " + available + " available)");
                continue;
            }
            part.reserve(item.getQuantity());
            parts.save(part);
            reservations.save(new PartReservation(UUID.randomUUID().toString(),
                    nextReservationNo(), workOrderId, item.getPartCode(), item.getQuantity(),
                    PartReservation.STATUS_RESERVED, Instant.now(clock)));
        }
        if (shortages.isEmpty()) {
            return null;
        }
        return "parts shortfall: " + String.join("; ", shortages);
    }

    private synchronized String nextWoNo() {
        return "WO-2026-" + String.format("%05d", workOrders.count() + 1);
    }

    private synchronized String nextReservationNo() {
        return "RES-2026-" + String.format("%05d", reservations.count() + 1);
    }

    public static List<WorkOrder> sortOldestFirst(List<WorkOrder> orders) {
        return orders.stream()
                .sorted(Comparator.comparing(WorkOrder::getOpenedAt))
                .toList();
    }
}
