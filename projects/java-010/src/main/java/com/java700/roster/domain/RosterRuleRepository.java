package com.java700.roster.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** RosterRule persistence. */
public interface RosterRuleRepository extends JpaRepository<RosterRule, String> {

    List<RosterRule> findByActiveTrue();
}
