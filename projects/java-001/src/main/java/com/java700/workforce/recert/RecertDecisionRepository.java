package com.java700.workforce.recert;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecertDecisionRepository extends JpaRepository<RecertDecision, String> {

    List<RecertDecision> findByCampaignId(String campaignId);

    List<RecertDecision> findByGrantId(String grantId);
}
