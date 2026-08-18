package com.java700.workforce.identity;

import com.java700.workforce.common.masking.Masked;
import java.time.Instant;
import java.util.List;

public final class UserApi {

    private UserApi() {
    }

    public record UserView(String id, String username, @Masked String email, String orgUnit,
                           String status, Instant certExpiresAt) {

        static UserView from(UserProfile u) {
            return new UserView(u.getId(), u.getUsername(), u.getEmail(), u.getOrgUnit(),
                    u.getStatus(), u.getCertExpiresAt());
        }
    }

    public record RoleAssignments(List<String> roles) {

        public RoleAssignments {
            roles = List.copyOf(roles);
        }
    }
}
