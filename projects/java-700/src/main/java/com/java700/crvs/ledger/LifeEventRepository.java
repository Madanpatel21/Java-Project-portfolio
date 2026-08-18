package com.java700.crvs.ledger;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LifeEventRepository extends JpaRepository<LifeEvent, Long> {

    Optional<LifeEvent> findTopByOrderByGlobalSeqDesc();

    List<LifeEvent> findByPersonIdOrderByChainSeqAsc(String personId);

    Optional<LifeEvent> findTopByPersonIdOrderByChainSeqDesc(String personId);
}
