package com.java700.expfraud.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Rule violation persistence. */
public interface RuleViolationRepository extends JpaRepository<RuleViolation, String> {

    List<RuleViolation> findByClaimIdOrderByCreatedAtAsc(String claimId);
}
