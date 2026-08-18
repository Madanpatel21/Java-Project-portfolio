package com.java700.stewardship.patients;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabValueRepository extends JpaRepository<LabValue, String> {

    List<LabValue> findByPatientIdAndTypeOrderByMeasuredAtDesc(String patientId, String type);
}
