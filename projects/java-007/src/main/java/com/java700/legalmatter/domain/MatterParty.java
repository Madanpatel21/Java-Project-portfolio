package com.java700.legalmatter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** An edge in the parties graph: a party's role on a matter. */
@Entity
@Table(name = "matter_parties")
public class MatterParty {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "matter_id", nullable = false, length = 36)
    private String matterId;

    @Column(name = "party_id", nullable = false, length = 36)
    private String partyId;

    @Column(name = "role", nullable = false, length = 16)
    private String role;

    protected MatterParty() {
    }

    public MatterParty(String id, String matterId, String partyId, String role, Instant ignored) {
        this.id = id;
        this.matterId = matterId;
        this.partyId = partyId;
        this.role = role;
    }

    public String getMatterId() {
        return matterId;
    }

    public String getPartyId() {
        return partyId;
    }

    public String getRole() {
        return role;
    }
}
