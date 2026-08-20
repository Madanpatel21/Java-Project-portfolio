package com.java700.expfraud.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Fraud case persistence. */
public interface FraudCaseRepository extends JpaRepository<FraudCase, String> {

    Optional<FraudCase> findByCaseNo(String caseNo);

    Optional<FraudCase> findByClaimId(String claimId);

    List<FraudCase> findByStatusOrderByOpenedAtAsc(String status);
}
