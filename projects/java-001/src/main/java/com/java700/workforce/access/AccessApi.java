package com.java700.workforce.access;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class AccessApi {

    private AccessApi() {
    }

    public record CreateRequest(
            @NotBlank String subjectUserId,
            @NotBlank @Size(max = 64) String resourceType,
            @NotBlank @Size(max = 120) String resourceName,
            @NotEmpty List<@NotBlank String> roles,
            @Size(max = 2000) String justification) {

        public CreateRequest {
            roles = List.copyOf(roles);
        }
    }

    public record DecideRequest(@Size(max = 1000) String comment) {
    }

    public record RequestView(String id, String requesterId, String subjectUserId, String resourceType,
                              String resourceName, List<String> roles, String justification,
                              String status, Instant createdAt, Instant decidedAt, String decidedBy) {

        public RequestView {
            roles = List.copyOf(roles);
        }

        static RequestView from(AccessRequest r) {
            return new RequestView(r.getId(), r.getRequesterId(), r.getSubjectUserId(),
                    r.getResourceType(), r.getResourceName(), AccessService.parseRoles(r.getRolesJson()),
                    r.getJustification(), r.getStatus().name(), r.getCreatedAt(), r.getDecidedAt(),
                    r.getDecidedBy());
        }
    }

    public record GrantView(String id, String userId, String resourceType, String resourceName,
                            List<String> roles, String status, Instant grantedAt, Instant expiresAt,
                            Instant recertDueAt, Instant recertifiedAt, Instant revokedAt,
                            String revokeReason) {

        public GrantView {
            roles = List.copyOf(roles);
        }

        static GrantView from(Grant g) {
            return new GrantView(g.getId(), g.getUserId(), g.getResourceType(), g.getResourceName(),
                    g.roles(), g.getStatus().name(), g.getGrantedAt(), g.getExpiresAt(),
                    g.getRecertDueAt(), g.getRecertifiedAt(), g.getRevokedAt(), g.getRevokeReason());
        }
    }

    public record RevokeRequest(@Size(max = 1000) String reason) {
    }
}
