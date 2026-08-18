package com.java700.workforce.recert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A reviewer's keep/revoke decision on one grant inside a campaign. */
@Entity
@Table(name = "recert_decision")
public class RecertDecision {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "campaign_id", nullable = false, length = 36)
    private String campaignId;

    @Column(name = "grant_id", nullable = false, length = 36)
    private String grantId;

    @Column(name = "decided_by", nullable = false, length = 120)
    private String decidedBy;

    @Column(name = "decision", nullable = false, length = 16)
    private String decision;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    protected RecertDecision() {
    }

    public RecertDecision(String id, String campaignId, String grantId, String decidedBy,
                          String decision, Instant decidedAt) {
        this.id = id;
        this.campaignId = campaignId;
        this.grantId = grantId;
        this.decidedBy = decidedBy;
        this.decision = decision;
        this.decidedAt = decidedAt;
    }

    public String getId() {
        return id;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public String getGrantId() {
        return grantId;
    }

    public String getDecision() {
        return decision;
    }
}
