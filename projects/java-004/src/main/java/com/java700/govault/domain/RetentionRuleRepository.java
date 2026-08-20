package com.java700.govault.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetentionRuleRepository extends JpaRepository<RetentionRule, String> {
    List<RetentionRule> findByActiveTrue();

        Optional<RetentionRule> findByRetentionClass(String retentionClass);
}
