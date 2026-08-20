package com.java700.wflow.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, String> {
    List<WorkflowInstance> findByStatus(String status);

        List<WorkflowInstance> findByStatusAndResumeAtBefore(String status, Instant at);

        Optional<WorkflowInstance> findByDefinitionIdAndBusinessKey(String definitionId, String businessKey);
}
