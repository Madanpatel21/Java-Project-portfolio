package com.java700.fleetmaint.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Part persistence. */
public interface PartRepository extends JpaRepository<Part, String> {

    Optional<Part> findByPartCode(String partCode);
}
