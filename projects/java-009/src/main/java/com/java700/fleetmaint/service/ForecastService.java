package com.java700.fleetmaint.service;

import com.java700.fleetmaint.common.audit.AuditLogService;
import com.java700.fleetmaint.domain.MaintenancePlan;
import com.java700.fleetmaint.domain.MaintenancePlanRepository;
import com.java700.fleetmaint.domain.MaintenanceTask;
import com.java700.fleetmaint.domain.MaintenanceTaskRepository;
import com.java700.fleetmaint.domain.Vehicle;
import com.java700.fleetmaint.domain.VehicleRepository;
import com.java700.fleetmaint.messaging.DomainEventBus;
import com.java700.fleetmaint.messaging.FleetEvents;
import com.java700.fleetmaint.observability.Metrics;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The meter/calendar scheduling engine.
 *
 * <p>For every active vehicle and applicable maintenance plan, computes the next service
 * due point (odometer or calendar) and maintains one open task per vehicle+plan:
 * OVERDUE when the due point has passed, DUE inside the service window, SCHEDULED inside
 * the forecast horizon. Compliance plans are prioritised.</p>
 */
@Service
public class ForecastService {

    private static final Logger log = LoggerFactory.getLogger(ForecastService.class);
    private static final int DUE_WINDOW_KM = 1500;
    private static final int FORECAST_HORIZON_KM = 3000;
    private static final int DUE_WINDOW_DAYS = 7;
    private static final int FORECAST_HORIZON_DAYS = 14;
    private static final List<String> OPEN_STATUSES = List.of(
            MaintenanceTask.STATUS_SCHEDULED, MaintenanceTask.STATUS_DUE,
            MaintenanceTask.STATUS_OVERDUE);

    private final VehicleRepository vehicles;
    private final MaintenancePlanRepository plans;
    private final MaintenanceTaskRepository tasks;
    private final DomainEventBus events;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;

    public ForecastService(VehicleRepository vehicles, MaintenancePlanRepository plans,
                           MaintenanceTaskRepository tasks, DomainEventBus events,
                           AuditLogService audit, Metrics metrics, Clock clock) {
        this.vehicles = vehicles;
        this.plans = plans;
        this.tasks = tasks;
        this.events = events;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
    }

    /** Runs a full forecast pass and returns what changed. */
    @Transactional
    public Api.ForecastResult runForecast(String username) {
        int created = 0;
        int updated = 0;
        int overdue = 0;
        for (Vehicle vehicle : vehicles.findByStatus(Vehicle.STATUS_ACTIVE)) {
            for (MaintenancePlan plan : plans.findByActiveTrue()) {
                if (!appliesTo(plan, vehicle)) {
                    continue;
                }
                Forecast forecast = computeForecast(plan, vehicle);
                MaintenanceTask task = findOpenTask(vehicle.getId(), plan.getId());
                if (task == null) {
                    if (forecast.status == null) {
                        continue;
                    }
                    MaintenanceTask newTask = new MaintenanceTask(UUID.randomUUID().toString(),
                            nextTaskNo(), vehicle.getId(), plan.getId(), forecast.dueType,
                            forecast.dueDate, forecast.dueOdometer, forecast.status,
                            priorityOf(plan), Instant.now(clock), null, null, Instant.now(clock));
                    tasks.save(newTask);
                    created++;
                    if (MaintenanceTask.STATUS_OVERDUE.equals(forecast.status)) {
                        overdue++;
                        metrics.taskOverdue();
                        events.publish(new FleetEvents.ServiceDue(UUID.randomUUID().toString(),
                                Instant.now(clock), newTask.getId(), newTask.getTaskNo(),
                                vehicle.getId(), vehicle.getPlate(), plan.getCode(), true));
                    }
                    metrics.taskForecasted();
                } else if (!forecast.status.equals(task.getStatus())
                        || !Objects.equals(forecast.dueDate, task.getDueDate())) {
                    task.markDue(forecast.status);
                    MaintenanceTask refreshed = new MaintenanceTask(task.getId(), task.getTaskNo(),
                            task.getVehicleId(), task.getPlanId(), task.getDueType(),
                            forecast.dueDate, forecast.dueOdometer, forecast.status,
                            task.getPriority(), task.getForecastAt(), task.getWorkOrderId(),
                            task.getCompletedAt(), task.getCreatedAt());
                    tasks.save(refreshed);
                    updated++;
                    if (MaintenanceTask.STATUS_OVERDUE.equals(forecast.status)) {
                        overdue++;
                        metrics.taskOverdue();
                    }
                }
            }
        }
        audit.record("FORECAST_RUN", "maintenance_tasks",
                "created=" + created + " updated=" + updated + " overdue=" + overdue,
                "by=" + username);
        log.info("Forecast pass: {} created, {} updated, {} overdue", created, updated, overdue);
        return new Api.ForecastResult(created, updated, overdue, dueSoon());
    }

    /** Open (non-completed) tasks across the fleet, most urgent first. */
    @Transactional(readOnly = true)
    public List<Api.TaskView> dueSoon() {
        return tasks.findByStatusInOrderByDueDateAsc(OPEN_STATUSES).stream()
                .sorted(Comparator.comparing(this::urgency).reversed())
                .map(this::toTaskView)
                .toList();
    }

    private int urgency(MaintenanceTask task) {
        int base = switch (task.getStatus()) {
            case MaintenanceTask.STATUS_OVERDUE -> 3;
            case MaintenanceTask.STATUS_DUE -> 2;
            default -> 1;
        };
        if (MaintenanceTask.PRIORITY_COMPLIANCE.equals(task.getPriority())) {
            base += 10;
        }
        return base;
    }

    private Forecast computeForecast(MaintenancePlan plan, Vehicle vehicle) {
        if (MaintenancePlan.INTERVAL_CALENDAR.equals(plan.getIntervalType())) {
            return calendarForecast(plan, vehicle);
        }
        return odometerForecast(plan, vehicle);
    }

    private Forecast odometerForecast(MaintenancePlan plan, Vehicle vehicle) {
        int anchor = lastCompletedOdometer(vehicle.getId(), plan.getId());
        int nextDue = anchor + plan.getIntervalValue();
        int remaining = nextDue - vehicle.getCurrentOdometer();
        Integer dueOdometer = nextDue;
        LocalDate dueDate = null;
        String status;
        if (remaining <= 0) {
            status = MaintenanceTask.STATUS_OVERDUE;
        } else if (remaining <= DUE_WINDOW_KM) {
            status = MaintenanceTask.STATUS_DUE;
        } else if (remaining <= FORECAST_HORIZON_KM) {
            status = MaintenanceTask.STATUS_SCHEDULED;
        } else {
            return new Forecast(null, null, null, null);
        }
        return new Forecast("ODOMETER", dueDate, dueOdometer, status);
    }

    private Forecast calendarForecast(MaintenancePlan plan, Vehicle vehicle) {
        LocalDate anchor = lastCompletedDate(vehicle.getId(), plan.getId());
        LocalDate nextDue = anchor.plusDays(plan.getIntervalValue());
        LocalDate today = LocalDate.now(clock);
        String status;
        if (nextDue.isBefore(today) || nextDue.isEqual(today)) {
            status = MaintenanceTask.STATUS_OVERDUE;
        } else if (!nextDue.isAfter(today.plusDays(DUE_WINDOW_DAYS))) {
            status = MaintenanceTask.STATUS_DUE;
        } else if (!nextDue.isAfter(today.plusDays(FORECAST_HORIZON_DAYS))) {
            status = MaintenanceTask.STATUS_SCHEDULED;
        } else {
            return new Forecast(null, null, null, null);
        }
        return new Forecast("CALENDAR", nextDue, null, status);
    }

    private int lastCompletedOdometer(String vehicleId, String planId) {
        return tasks.findByVehicleIdAndPlanIdOrderByCompletedAtDesc(vehicleId, planId).stream()
                .filter(task -> task.getCompletedAt() != null)
                .filter(task -> task.getDueOdometer() != null)
                .mapToInt(MaintenanceTask::getDueOdometer)
                .max()
                .orElseGet(() -> vehicles.findById(vehicleId)
                        .map(vehicle -> vehicle.getServiceAnchorOdometer() != null
                                ? vehicle.getServiceAnchorOdometer()
                                : vehicle.getCurrentOdometer())
                        .orElse(0));
    }

    private LocalDate lastCompletedDate(String vehicleId, String planId) {
        return tasks.findByVehicleIdAndPlanIdOrderByCompletedAtDesc(vehicleId, planId).stream()
                .filter(task -> task.getCompletedAt() != null)
                .map(task -> LocalDate.ofInstant(task.getCompletedAt(), clock.getZone()))
                .max(LocalDate::compareTo)
                .orElseGet(() -> {
                    Vehicle vehicle = vehicles.findById(vehicleId).orElse(null);
                    if (vehicle != null && vehicle.getLastServiceDate() != null) {
                        return vehicle.getLastServiceDate();
                    }
                    if (vehicle != null && vehicle.getPurchaseDate() != null) {
                        return vehicle.getPurchaseDate();
                    }
                    return LocalDate.now(clock);
                });
    }

    private MaintenanceTask findOpenTask(String vehicleId, String planId) {
        return tasks.findByVehicleIdAndStatusIn(vehicleId, OPEN_STATUSES).stream()
                .filter(task -> planId.equals(task.getPlanId()))
                .findFirst()
                .orElse(null);
    }

    private boolean appliesTo(MaintenancePlan plan, Vehicle vehicle) {
        return "ANY".equals(plan.getAppliesToCategory())
                || plan.getAppliesToCategory().equals(vehicle.getCategory());
    }

    private String priorityOf(MaintenancePlan plan) {
        if (plan.isComplianceRequired()) {
            return MaintenanceTask.PRIORITY_COMPLIANCE;
        }
        if ("BRAKE-INSPECT".equals(plan.getCode())) {
            return MaintenanceTask.PRIORITY_SAFETY;
        }
        return MaintenanceTask.PRIORITY_ROUTINE;
    }

    private Api.TaskView toTaskView(MaintenanceTask task) {
        Vehicle vehicle = vehicles.findById(task.getVehicleId()).orElse(null);
        MaintenancePlan plan = plans.findById(task.getPlanId()).orElse(null);
        return new Api.TaskView(task.getId(), task.getTaskNo(), task.getVehicleId(),
                vehicle == null ? "?" : vehicle.getPlate(),
                plan == null ? "?" : plan.getCode(), plan == null ? "?" : plan.getName(),
                task.getDueType(), task.getDueDate(), task.getDueOdometer(), task.getStatus(),
                task.getPriority(), task.getForecastAt(), task.getWorkOrderId(),
                task.getCompletedAt());
    }

    private synchronized String nextTaskNo() {
        long count = tasks.count();
        return "MT-2026-" + String.format("%05d", count + 1);
    }

    private record Forecast(String dueType, LocalDate dueDate, Integer dueOdometer, String status) {
    }
}
