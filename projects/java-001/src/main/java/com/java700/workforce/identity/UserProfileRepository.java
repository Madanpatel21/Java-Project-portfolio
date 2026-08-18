package com.java700.workforce.identity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    Optional<UserProfile> findByUsername(String username);

    Page<UserProfile> findByOrgUnit(String orgUnit, Pageable pageable);

    Page<UserProfile> findByUsernameContainingIgnoreCase(String query, Pageable pageable);

    List<UserProfile> findByStatus(String status);
}
