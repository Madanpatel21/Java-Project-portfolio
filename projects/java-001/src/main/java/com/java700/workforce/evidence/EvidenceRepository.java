package com.java700.workforce.evidence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<EvidenceEntry, Long> {

    Optional<EvidenceEntry> findTopByOrderBySeqDesc();

    List<EvidenceEntry> findByAggregateTypeAndAggregateIdOrderBySeqAsc(
            String aggregateType, String aggregateId);
}
