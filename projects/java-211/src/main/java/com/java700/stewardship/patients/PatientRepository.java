package com.java700.stewardship.patients;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, String> {

    Optional<Patient> findByMrn(String mrn);

    Page<Patient> findByMrnContainingIgnoreCaseOrNameContainingIgnoreCase(
            String mrn, String name, Pageable pageable);
}
