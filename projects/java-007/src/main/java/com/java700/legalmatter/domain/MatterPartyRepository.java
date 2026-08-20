package com.java700.legalmatter.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatterPartyRepository extends JpaRepository<MatterParty, String> {
    List<MatterParty> findByMatterId(String matterId);

        List<MatterParty> findByPartyId(String partyId);
}
