package com.java700.p2p.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxRecord, String> {
    List<OutboxRecord> findByStatusOrderByCreatedAtAsc(String status);
}
