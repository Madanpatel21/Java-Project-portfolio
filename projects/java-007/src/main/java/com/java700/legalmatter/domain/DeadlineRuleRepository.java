package com.java700.legalmatter.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadlineRuleRepository extends JpaRepository<DeadlineRule, String> {
    List<DeadlineRule> findByActiveTrue();
}
