package com.java700.p2p.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToleranceRuleRepository extends JpaRepository<ToleranceRule, String> {
    List<ToleranceRule> findByActiveTrue();
}
