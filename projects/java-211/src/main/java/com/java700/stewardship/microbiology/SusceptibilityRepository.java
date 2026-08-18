package com.java700.stewardship.microbiology;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SusceptibilityRepository extends JpaRepository<SusceptibilityResult, String> {

    List<SusceptibilityResult> findByIsolateId(String isolateId);

    @Query("""
            select s from SusceptibilityResult s
            where s.isolateId in
              (select i.id from Isolate i where i.cultureId in
                (select c.id from Culture c where c.patientId = :patientId))
            """)
    List<SusceptibilityResult> findByPatient(String patientId);
}
