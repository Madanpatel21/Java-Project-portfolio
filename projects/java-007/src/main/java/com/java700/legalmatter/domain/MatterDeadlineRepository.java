package com.java700.legalmatter.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatterDeadlineRepository extends JpaRepository<MatterDeadline, String> {
    List<MatterDeadline> findByMatterIdOrderByDueAtAsc(String matterId);

        List<MatterDeadline> findByStatusAndDueAtBefore(String status, LocalDate at);
}
