package com.java700.stewardship.microbiology;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CultureRepository extends JpaRepository<Culture, String> {

    List<Culture> findByPatientId(String patientId);
}
