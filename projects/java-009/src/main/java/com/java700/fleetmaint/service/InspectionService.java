package com.java700.fleetmaint.service;

import com.java700.fleetmaint.common.api.Problems;
import com.java700.fleetmaint.common.audit.AuditLogService;
import com.java700.fleetmaint.domain.Inspection;
import com.java700.fleetmaint.domain.InspectionRepository;
import com.java700.fleetmaint.domain.MaintenancePlan;
import com.java700.fleetmaint.domain.MaintenancePlanRepository;
import com.java700.fleetmaint.domain.Vehicle;
import com.java700.fleetmaint.domain.VehicleRepository;
import com.java700.fleetmaint.messaging.DomainEventBus;
import com.java700.fleetmaint.messaging.FleetEvents;
import com.java700.fleetmaint.observability.Metrics;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compliance inspection ledger. A FAIL puts the vehicle on COMPLIANCE_HOLD until a
 * subsequent PASS. The compliance report joins every vehicle to its latest inspection
 * and the calendar plans that require it.
 */
@Service
public class InspectionService {

    private final InspectionRepository inspections;
    private final VehicleRepository vehicles;
    private final MaintenancePlanRepository plans;
    private final DomainEventBus events;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;

    public InspectionService(InspectionRepository inspections, VehicleRepository vehicles,
                             MaintenancePlanRepository plans, DomainEventBus events,
                             AuditLogService audit, Metrics metrics, Clock clock) {
        this.inspections = inspections;
        this.vehicles = vehicles;
        this.plans = plans;
        this.events = events;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
    }

    @Transactional
    public Inspection record(String vehicleId, Api.InspectionRequest request, String username) {
        if (!Inspection.RESULT_PASS.equals(request.result())
                && !Inspection.RESULT_FAIL.equals(request.result())) {
            throw new Problems.BadRequest("result must be PASS or FAIL");
        }
        Vehicle vehicle = vehicles.findById(vehicleId)
                .orElseThrow(() -> new Problems.NotFound("vehicle " + vehicleId));
        Inspection inspection = new Inspection(UUID.randomUUID().toString(), nextInspectionNo(),
                vehicleId, request.inspectionType(), request.inspector(), request.result(),
                trimToNull(request.notes()), request.validUntil(), Instant.now(clock));
        Inspection saved = inspections.save(inspection);
        if (Inspection.RESULT_FAIL.equals(saved.getResult())) {
            vehicle.transition(Vehicle.STATUS_COMPLIANCE_HOLD);
            metrics.inspectionFailed();
            events.publish(new FleetEvents.InspectionFailed(UUID.randomUUID().toString(),
                    Instant.now(clock), vehicleId, vehicle.getPlate(), saved.getInspectionType()));
            audit.record("INSPECTION_FAILED", "vehicle", vehicle.getPlate(),
                    "type=" + saved.getInspectionType() + " by=" + username);
        } else if (Vehicle.STATUS_COMPLIANCE_HOLD.equals(vehicle.getStatus())) {
            vehicle.transition(Vehicle.STATUS_ACTIVE);
            audit.record("COMPLIANCE_RESTORED", "vehicle", vehicle.getPlate(),
                    "type=" + saved.getInspectionType() + " by=" + username);
        }
        vehicles.save(vehicle);
        audit.record("INSPECTION_RECORDED", "inspection", saved.getInspectionNo(),
                "vehicle=" + vehicle.getPlate() + " result=" + saved.getResult()
                        + " by=" + username);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Inspection> history(String vehicleId) {
        return inspections.findByVehicleIdOrderByPerformedAtDesc(vehicleId);
    }

    /** Vehicles with active compliance plans: latest inspection vs validity window. */
    @Transactional(readOnly = true)
    public List<Api.ComplianceRow> complianceReport() {
        List<Api.ComplianceRow> rows = new ArrayList<>();
        for (MaintenancePlan plan : plans.findByActiveTrue()) {
            if (!plan.isComplianceRequired()) {
                continue;
            }
            for (Vehicle vehicle : vehicles.findAll()) {
                if (!"ANY".equals(plan.getAppliesToCategory())
                        && !plan.getAppliesToCategory().equals(vehicle.getCategory())) {
                    continue;
                }
                Inspection latest = inspections.findByVehicleIdOrderByPerformedAtDesc(
                        vehicle.getId()).stream().findFirst().orElse(null);
                boolean compliant = latest != null
                        && Inspection.RESULT_PASS.equals(latest.getResult())
                        && latest.getValidUntil() != null
                        && !latest.getValidUntil().isBefore(LocalDate.now(clock));
                rows.add(new Api.ComplianceRow(vehicle.getId(), vehicle.getPlate(),
                        plan.getCode(), latest == null ? "NONE" : latest.getInspectionNo(),
                        latest == null ? null : latest.getValidUntil(), compliant));
            }
        }
        return rows.stream()
                .sorted(Comparator.comparing(Api.ComplianceRow::compliant)
                        .thenComparing(Api.ComplianceRow::plate))
                .toList();
    }

    public Api.InspectionView view(Inspection inspection) {
        Vehicle vehicle = vehicles.findById(inspection.getVehicleId()).orElse(null);
        return new Api.InspectionView(inspection.getId(), inspection.getInspectionNo(),
                inspection.getVehicleId(), vehicle == null ? "?" : vehicle.getPlate(),
                inspection.getInspectionType(), inspection.getInspector(), inspection.getResult(),
                inspection.getNotes(), inspection.getValidUntil(), inspection.getPerformedAt());
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private synchronized String nextInspectionNo() {
        return "INSP-2026-" + String.format("%05d", inspections.count() + 1);
    }
}
