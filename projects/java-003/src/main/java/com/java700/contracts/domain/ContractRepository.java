package com.java700.contracts.domain;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, String> {
    Optional<Contract> findByContractNo(String contractNo);

        Page<Contract> findByStatus(String status, Pageable pageable);
}
