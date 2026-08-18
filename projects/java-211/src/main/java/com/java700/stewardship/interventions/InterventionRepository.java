package com.java700.stewardship.interventions;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterventionRepository extends JpaRepository<Intervention, String> {

    List<Intervention> findByPrescriptionIdOrderByProposedAtDesc(String prescriptionId);

    List<Intervention> findByStatus(String status);

    List<Intervention> findByStatusAndProposedAtBefore(String status, Instant at);
}
