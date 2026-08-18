package com.java700.stewardship.prescriptions;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

    List<Prescription> findByPatientIdAndStatus(String patientId, String status);

    List<Prescription> findByStatus(String status);

    List<Prescription> findByStatusAndStartAtBefore(String status, java.time.Instant at);
}
