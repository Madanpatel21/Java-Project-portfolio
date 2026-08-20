package com.java700.expfraud.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Peer baseline persistence. */
public interface PeerBaselineRepository extends JpaRepository<PeerBaseline, String> {

    Optional<PeerBaseline> findByDepartmentAndCategory(String department, String category);
}
