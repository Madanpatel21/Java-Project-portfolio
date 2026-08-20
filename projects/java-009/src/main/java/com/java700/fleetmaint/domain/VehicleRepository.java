package com.java700.fleetmaint.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Vehicle persistence. */
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    List<Vehicle> findByStatus(String status);

    Optional<Vehicle> findByPlate(String plate);

    List<Vehicle> findByCategory(String category);
}
