package com.java700.workforce.recert;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecertCampaignRepository extends JpaRepository<RecertCampaign, String> {

    List<RecertCampaign> findByStatus(String status);
}
