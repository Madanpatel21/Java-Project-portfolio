package com.java700.fleetmaint.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** MaintenanceTask persistence. */
public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, String> {

    List<MaintenanceTask> findByStatusInOrderByDueDateAsc(List<String> statuses);

    List<MaintenanceTask> findByVehicleIdAndStatusIn(String vehicleId, List<String> statuses);

    List<MaintenanceTask> findByVehicleIdAndPlanIdOrderByCompletedAtDesc(String vehicleId, String planId);
}
