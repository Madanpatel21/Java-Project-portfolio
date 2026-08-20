package com.java700.achain.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalDecisionRepository extends JpaRepository<ApprovalDecision, String> {
    List<ApprovalDecision> findByRequestIdOrderByStepNoAsc(String requestId);
}
