package com.java700.contracts.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractVersionRepository extends JpaRepository<ContractVersion, String> {
    List<ContractVersion> findByContractIdOrderByVersionNoDesc(String contractId);

        Optional<ContractVersion> findByContractIdAndVersionNo(String contractId, int versionNo);
}
