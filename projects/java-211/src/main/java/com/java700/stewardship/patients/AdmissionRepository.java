package com.java700.stewardship.patients;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmissionRepository extends JpaRepository<Admission, String> {

    List<Admission> findByPatientIdOrderByAdmittedAtDesc(String patientId);

    List<Admission> findByAdmittedAtBeforeAndDischargedAtAfter(Instant at, Instant at2);
}
