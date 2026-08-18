package com.java700.stewardship.guidelines;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class GuidelineApi {

    private GuidelineApi() {
    }

    public record GuidelineView(String id, String name, int versionNo, String status,
                                Instant effectiveFrom, String createdBy, String rulesJson) {

        static GuidelineView from(StewardshipGuideline g) {
            return new GuidelineView(g.getId(), g.getName(), g.getVersionNo(), g.getStatus(),
                    g.getEffectiveFrom(), g.getCreatedBy(), g.getRulesJson());
        }
    }

    public record CreateVersionRequest(@NotBlank String rulesJson) {
    }
}
