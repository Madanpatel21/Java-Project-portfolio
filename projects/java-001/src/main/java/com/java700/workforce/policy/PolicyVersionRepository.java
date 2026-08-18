package com.java700.workforce.policy;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyVersionRepository extends JpaRepository<PolicyVersion, String> {

    List<PolicyVersion> findByPolicyIdOrderByVersionNoDesc(String policyId);

    Optional<PolicyVersion> findByPolicyIdAndStatus(String policyId, String status);
}
