package com.java700.workforce.compliance;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViolationRepository extends JpaRepository<Violation, String> {

    Optional<Violation> findByUserIdAndPolicyCodeAndRuleTypeAndStatusIn(
            String userId, String policyCode, String ruleType, List<String> statuses);

    Page<Violation> findByStatus(String status, Pageable pageable);

    Page<Violation> findByUserId(String userId, Pageable pageable);

    long countByStatus(String status);
}
