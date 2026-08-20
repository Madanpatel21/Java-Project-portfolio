package com.java700.contracts.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObligationEventRepository extends JpaRepository<ObligationEvent, String> {
    List<ObligationEvent> findByObligationIdOrderByOccurredAtAsc(String obligationId);
}
