package com.java700.wflow.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, String> {
    List<WorkflowDefinition> findByDefinitionKeyOrderByVersionNoDesc(String key);

        Optional<WorkflowDefinition> findByDefinitionKeyAndStatus(String key, String status);
}
