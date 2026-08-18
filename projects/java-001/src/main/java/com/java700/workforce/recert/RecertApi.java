package com.java700.workforce.recert;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class RecertApi {

    private RecertApi() {
    }

    public record CampaignView(String id, String name, Instant windowStart, Instant windowEnd,
                               String status, String generatedBy, Instant generatedAt) {

        static CampaignView from(RecertCampaign c) {
            return new CampaignView(c.getId(), c.getName(), c.getWindowStart(), c.getWindowEnd(),
                    c.getStatus(), c.getGeneratedBy(), c.getGeneratedAt());
        }
    }

    public record GenerateCampaignRequest(@NotBlank String name, int windowDays) {
    }

    public record DecideRequest(@NotBlank String grantId, @NotBlank String decision) {
    }

    public record DecisionView(String id, String campaignId, String grantId, String decidedBy,
                               String decision, Instant decidedAt) {

        static DecisionView from(RecertDecision d) {
            return new DecisionView(d.getId(), d.getCampaignId(), d.getGrantId(), d.getDecidedBy(),
                    d.getDecision(), d.getDecidedAt());
        }
    }
}
