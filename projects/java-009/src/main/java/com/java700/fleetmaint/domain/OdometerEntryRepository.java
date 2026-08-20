package com.java700.fleetmaint.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** OdometerEntry persistence. */
public interface OdometerEntryRepository extends JpaRepository<OdometerEntry, String> {

    List<OdometerEntry> findByVehicleIdOrderByRecordedAtDesc(String vehicleId);
}
