package com.java700.workforce.policy;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class PolicyApi {

    private PolicyApi() {
    }

    public record PolicyView(String id, String code, String name, String description,
                             String activeVersionId) {

        static PolicyView from(Policy p) {
            return new PolicyView(p.getId(), p.getCode(), p.getName(), p.getDescription(),
                    p.getActiveVersionId());
        }
    }

    public record VersionView(String id, String policyId, int versionNo, String rulesJson,
                              String status, Instant effectiveFrom, String createdBy) {

        static VersionView from(PolicyVersion v) {
            return new VersionView(v.getId(), v.getPolicyId(), v.getVersionNo(), v.getRulesJson(),
                    v.getStatus(), v.getEffectiveFrom(), v.getCreatedBy());
        }
    }

    public record CreateVersionRequest(@NotBlank String rulesJson) {
    }
}
