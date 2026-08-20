package com.java700.roster.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Availability persistence. */
public interface AvailabilityRepository extends JpaRepository<Availability, String> {

    List<Availability> findByEmployeeId(String employeeId);
}
