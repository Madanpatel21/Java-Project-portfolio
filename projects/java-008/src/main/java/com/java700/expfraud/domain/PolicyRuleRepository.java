package com.java700.expfraud.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Policy rule persistence. */
public interface PolicyRuleRepository extends JpaRepository<PolicyRule, String> {

    List<PolicyRule> findByActiveTrueOrderBySortOrderAsc();
}
