package com.java700.contracts.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, String> {
    List<Approval> findByTargetTypeAndTargetId(String targetType, String targetId);
}
