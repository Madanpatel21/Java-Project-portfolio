package com.java700.wflow.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, String> {
    List<WorkflowStep> findByInstanceIdOrderByOccurredAtAsc(String instanceId);
}
