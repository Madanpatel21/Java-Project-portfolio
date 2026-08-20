package com.java700.expfraud.common.web;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {

    Optional<IdempotencyRecord> findByKey(String key);

    void deleteByCreatedAtBefore(Instant threshold);
}
