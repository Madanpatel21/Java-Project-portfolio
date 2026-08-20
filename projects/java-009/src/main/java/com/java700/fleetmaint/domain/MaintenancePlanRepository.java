package com.java700.fleetmaint.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** MaintenancePlan persistence. */
public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, String> {

    List<MaintenancePlan> findByActiveTrue();

    Optional<MaintenancePlan> findByCode(String code);
}
