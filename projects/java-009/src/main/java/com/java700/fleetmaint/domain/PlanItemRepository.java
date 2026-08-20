package com.java700.fleetmaint.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** PlanItem persistence. */
public interface PlanItemRepository extends JpaRepository<PlanItem, String> {

    List<PlanItem> findByPlanId(String planId);
}
