package com.java700.achain.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalChainRepository extends JpaRepository<ApprovalChain, String> {
    Optional<ApprovalChain> findByChainCode(String code);
}
