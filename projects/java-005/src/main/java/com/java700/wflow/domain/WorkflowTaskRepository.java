package com.java700.wflow.domain;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, String> {
    List<WorkflowTask> findByInstanceIdOrderByCreatedAtAsc(String instanceId);

    org.springframework.data.domain.Page<WorkflowTask> findByStatusAndAssigneeRole(
            String status, String role, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<WorkflowTask> findByStatus(
            String status, org.springframework.data.domain.Pageable pageable);

        List<WorkflowTask> findByStatusAndAssigneeRole(String status, String role);

        List<WorkflowTask> findByStatusAndDueAtBefore(String status, Instant at);

        List<WorkflowTask> findByInstanceIdAndStatus(String instanceId, String status);
}
