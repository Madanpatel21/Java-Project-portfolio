package com.java700.crvs.registration;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, String> {

    Page<Registration> findByStatus(String status, Pageable pageable);

    Page<Registration> findByStatusAndOfficeId(String status, String officeId, Pageable pageable);

    List<Registration> findByPersonIdOrderByCreatedAtDesc(String personId);

    long countByStatus(String status);
}
