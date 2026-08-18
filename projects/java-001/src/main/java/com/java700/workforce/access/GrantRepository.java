package com.java700.workforce.access;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrantRepository extends JpaRepository<Grant, String> {

    List<Grant> findByStatus(String status);

    List<Grant> findByUserIdAndStatus(String userId, String status);

    Page<Grant> findByUserId(String userId, Pageable pageable);

    List<Grant> findByStatusAndExpiresAtBefore(String status, Instant now);

    Optional<Grant> findByIdAndStatus(String id, String status);
}
