package com.java700.fleetmaint.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Inspection persistence. */
public interface InspectionRepository extends JpaRepository<Inspection, String> {

    List<Inspection> findByVehicleIdOrderByPerformedAtDesc(String vehicleId);
}
