package com.java700.expfraud.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Duplicate group persistence. */
public interface DuplicateGroupRepository extends JpaRepository<DuplicateGroup, String> {

    Optional<DuplicateGroup> findByGroupKey(String groupKey);
}
