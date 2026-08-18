package com.java700.workforce.policy;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, String> {

    Optional<Policy> findByCode(String code);
}
