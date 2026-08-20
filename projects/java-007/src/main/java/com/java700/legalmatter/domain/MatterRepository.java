package com.java700.legalmatter.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatterRepository extends JpaRepository<Matter, String> {
    Optional<Matter> findByMatterNo(String matterNo);
}
