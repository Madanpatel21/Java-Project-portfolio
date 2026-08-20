package com.java700.roster.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** SwapRequest persistence. */
public interface SwapRequestRepository extends JpaRepository<SwapRequest, String> {

    List<SwapRequest> findByStatusOrderByCreatedAtAsc(String status);

    List<SwapRequest> findByRequestedByOrderByCreatedAtDesc(String employeeId);
}
