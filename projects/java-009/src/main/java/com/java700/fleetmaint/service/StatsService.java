package com.java700.fleetmaint.service;

import com.java700.fleetmaint.domain.MaintenanceTask;
import com.java700.fleetmaint.domain.MaintenanceTaskRepository;
import com.java700.fleetmaint.domain.OdometerEntry;
import com.java700.fleetmaint.domain.OdometerEntryRepository;
import com.java700.fleetmaint.domain.Vehicle;
import com.java700.fleetmaint.domain.VehicleRepository;
import com.java700.fleetmaint.domain.WorkOrder;
import com.java700.fleetmaint.domain.WorkOrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fleet-wide SLA and cost analytics per asset. */
@Service
public class StatsService {

    private final VehicleRepository vehicles;
    private final MaintenanceTaskRepository tasks;
    private final WorkOrderRepository workOrders;
    private final OdometerEntryRepository odometerEntries;

    public StatsService(VehicleRepository vehicles, MaintenanceTaskRepository tasks,
                        WorkOrderRepository workOrders, OdometerEntryRepository odometerEntries) {
        this.vehicles = vehicles;
        this.tasks = tasks;
        this.workOrders = workOrders;
        this.odometerEntries = odometerEntries;
    }

    @Transactional(readOnly = true)
    public Api.StatsView stats() {
        int activeVehicles = (int) vehicles.findAll().stream()
                .filter(v -> Vehicle.STATUS_ACTIVE.equals(v.getStatus())
                        || Vehicle.STATUS_IN_SHOP.equals(v.getStatus()))
                .count();
        int due = tasks.findByStatusInOrderByDueDateAsc(List.of(MaintenanceTask.STATUS_DUE,
                MaintenanceTask.STATUS_SCHEDULED)).size();
        int overdue = tasks.findByStatusInOrderByDueDateAsc(
                List.of(MaintenanceTask.STATUS_OVERDUE)).size();
        List<WorkOrder> open = workOrders.findByStatus(WorkOrder.STATUS_OPEN);
        List<WorkOrder> hold = workOrders.findByStatus(WorkOrder.STATUS_PARTS_HOLD);
        List<WorkOrder> completed = workOrders.findByStatus(WorkOrder.STATUS_COMPLETED);

        BigDecimal totalCost = BigDecimal.ZERO;
        Map<String, List<BigDecimal>> perVehicle = new HashMap<>();
        for (WorkOrder workOrder : completed) {
            BigDecimal cost = BigDecimal.ZERO;
            if (workOrder.getLaborCost() != null) {
                cost = cost.add(workOrder.getLaborCost());
            }
            if (workOrder.getPartsCost() != null) {
                cost = cost.add(workOrder.getPartsCost());
            }
            totalCost = totalCost.add(cost);
            perVehicle.computeIfAbsent(workOrder.getVehicleId(), ignored -> new ArrayList<>())
                    .add(cost);
        }
        List<Api.CostPerAsset> costPerAsset = new ArrayList<>();
        for (Map.Entry<String, List<BigDecimal>> entry : perVehicle.entrySet()) {
            Vehicle vehicle = vehicles.findById(entry.getKey()).orElse(null);
            BigDecimal sum = entry.getValue().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            costPerAsset.add(new Api.CostPerAsset(entry.getKey(),
                    vehicle == null ? "?" : vehicle.getPlate(), entry.getValue().size(), sum));
        }
        costPerAsset.sort(Comparator.comparing(Api.CostPerAsset::totalCost).reversed());
        int tamperFlags = (int) odometerEntries.findAll().stream()
                .filter(entry -> OdometerEntry.FLAG_SUSPICIOUS.equals(entry.getFlag()))
                .count();
        return new Api.StatsView(activeVehicles, due, overdue, open.size(), hold.size(),
                completed.size(), totalCost, costPerAsset, tamperFlags);
    }
}
