package com.java700.workforce.access;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, String> {

    List<Approval> findByAccessRequestId(String accessRequestId);
}
