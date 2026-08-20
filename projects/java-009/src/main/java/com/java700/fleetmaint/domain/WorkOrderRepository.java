package com.java700.fleetmaint.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** WorkOrder persistence. */
public interface WorkOrderRepository extends JpaRepository<WorkOrder, String> {

    List<WorkOrder> findByStatusOrderByOpenedAtAsc(String status);

    List<WorkOrder> findByStatus(String status);
}
