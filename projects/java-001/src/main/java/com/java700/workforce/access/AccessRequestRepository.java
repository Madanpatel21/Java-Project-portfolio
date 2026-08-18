package com.java700.workforce.access;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, String> {

    Page<AccessRequest> findByStatus(String status, Pageable pageable);

    Page<AccessRequest> findBySubjectUserId(String userId, Pageable pageable);
}
