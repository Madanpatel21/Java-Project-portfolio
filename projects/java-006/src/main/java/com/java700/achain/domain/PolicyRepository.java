package com.java700.achain.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, String> {
    Optional<Policy> findByPolicyCode(String code);
}
