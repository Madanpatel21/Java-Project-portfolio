package com.java700.govault.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalHoldRepository extends JpaRepository<LegalHold, String> {
    List<LegalHold> findByStatus(String status);
}
