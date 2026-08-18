package com.java700.workforce.audit;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportJobRepository extends JpaRepository<ExportJob, String> {

    List<ExportJob> findByStatusAndCreatedAtBefore(String status, Instant threshold);
}
