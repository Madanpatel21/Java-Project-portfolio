package com.java700.fleetmaint.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** API request/response records (immutable, defensive copies on collections). */
public final class Api {

    private Api() {
    }

    public record RegisterVehicleRequest(String vin, String plate, String make, String model,
                                         int modelYear, String category, int initialOdometer,
                                         Integer serviceAnchorOdometer, LocalDate lastServiceDate,
                                         String department, String driverName,
                                         LocalDate purchaseDate) {
    }

    public record VehicleView(String id, String vin, String plate, String make, String model,
                              int modelYear, String category, String status, int currentOdometer,
                              Instant odometerUpdatedAt, String department, String driverName) {
    }

    public record OdometerRequest(int reading, String source) {
    }

    public record OdometerResult(boolean accepted, String flag, String message, int newOdometer) {
    }

    public record OdometerEntryView(String id, String vehicleId, int reading, String source,
                                    String flag, String recordedBy, Instant recordedAt) {
    }

    public record PlanRequest(String code, String name, String appliesToCategory,
                              String intervalType, int intervalValue, boolean complianceRequired,
                              List<PlanItemRequest> items) {
        public PlanRequest {
            items = List.copyOf(items);
        }

        @Override
        public List<PlanItemRequest> items() {
            return List.copyOf(items);
        }
    }

    public record PlanItemRequest(String partCode, String partName, int quantity,
                                  BigDecimal estimatedCost) {
    }

    public record PlanView(String id, String code, String name, String appliesToCategory,
                           String intervalType, int intervalValue, boolean complianceRequired,
                           boolean active, List<PlanItemView> items) {
        public PlanView {
            items = List.copyOf(items);
        }

        @Override
        public List<PlanItemView> items() {
            return List.copyOf(items);
        }
    }

    public record PlanItemView(String partCode, String partName, int quantity,
                               BigDecimal estimatedCost) {
    }

    public record TaskView(String id, String taskNo, String vehicleId, String plate,
                           String planCode, String planName, String dueType, LocalDate dueDate,
                           Integer dueOdometer, String status, String priority, Instant forecastAt,
                           String workOrderId, Instant completedAt) {
    }

    public record ForecastResult(int created, int updated, int overdue, List<TaskView> dueSoon) {
        public ForecastResult {
            dueSoon = List.copyOf(dueSoon);
        }

        @Override
        public List<TaskView> dueSoon() {
            return List.copyOf(dueSoon);
        }
    }

    public record WorkOrderStartRequest(String mechanic) {
    }

    public record WorkOrderCompleteRequest(String mechanic, BigDecimal laborHours,
                                           BigDecimal laborCost, Integer odometerAtService,
                                           String note) {
    }

    public record WorkOrderView(String id, String woNo, String taskId, String taskNo,
                                String vehicleId, String plate, String status, String openedBy,
                                String mechanic, BigDecimal laborHours, BigDecimal laborCost,
                                BigDecimal partsCost, BigDecimal totalCost, String notes,
                                String shortfallReason, Integer odometerAtService, Instant openedAt,
                                Instant completedAt, List<ReservationView> reservations) {
        public WorkOrderView {
            reservations = List.copyOf(reservations);
        }

        @Override
        public List<ReservationView> reservations() {
            return List.copyOf(reservations);
        }
    }

    public record ReservationView(String reservationNo, String partCode, String partName,
                                  int quantity, String status) {
    }

    public record RestockRequest(int quantity) {
    }

    public record PartView(String id, String partCode, String name, int quantityOnHand,
                           int reservedQty, int reorderPoint, BigDecimal unitCost,
                           boolean needsReorder) {
    }

    public record InspectionRequest(String inspectionType, String inspector, String result,
                                    String notes, LocalDate validUntil) {
    }

    public record InspectionView(String id, String inspectionNo, String vehicleId, String plate,
                                 String inspectionType, String inspector, String result,
                                 String notes, LocalDate validUntil, Instant performedAt) {
    }

    public record ComplianceRow(String vehicleId, String plate, String planCode,
                                String lastInspection, LocalDate validUntil, boolean compliant) {
    }

    public record StatsView(int activeVehicles, int dueTasks, int overdueTasks, int openWorkOrders,
                            int partsHoldOrders, long completedOrders, BigDecimal totalMaintenanceCost,
                            List<CostPerAsset> costPerAsset, int tamperFlags) {
        public StatsView {
            costPerAsset = List.copyOf(costPerAsset);
        }

        @Override
        public List<CostPerAsset> costPerAsset() {
            return List.copyOf(costPerAsset);
        }
    }

    public record CostPerAsset(String vehicleId, String plate, long completedOrders,
                               BigDecimal totalCost) {
    }
}
