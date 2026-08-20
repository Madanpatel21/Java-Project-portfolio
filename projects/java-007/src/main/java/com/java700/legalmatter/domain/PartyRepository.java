package com.java700.legalmatter.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party, String> {
    List<Party> findByNormalizedName(String normalizedName);

        List<Party> findByActiveTrue();
}
