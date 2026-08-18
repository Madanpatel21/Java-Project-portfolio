package com.java700.workforce.bootstrap;




import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.workforce.access.GrantRepository;
import com.java700.workforce.access.Grant;
import com.java700.workforce.events.AccessEventRepository;
import com.java700.workforce.events.AccessEvent;
import com.java700.workforce.identity.UserProfile;
import com.java700.workforce.identity.UserProfileRepository;
import com.java700.workforce.recert.RecertCampaign;
import com.java700.workforce.recert.RecertCampaignRepository;
import com.java700.workforce.security.LocalUser;
import com.java700.workforce.security.LocalUserService;
import com.java700.workforce.security.Roles;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Dev-profile demo dataset: users, roles, grants (including deliberate policy violations), events. */
@Configuration
@Profile("dev")
public class SeedData {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);
    private static final String PASSWORD = "Password123!";

    @Bean
    CommandLineRunner seed(LocalUserService localUsers, UserProfileRepository profiles,
                           GrantRepository grants, AccessEventRepository events,
                           RecertCampaignRepository campaigns, PasswordEncoder encoder,
                           ObjectMapper mapper, Clock clock) {
        return args -> {
            if (profiles.count() > 0) {
                return; // already seeded (e.g. restarted with same H2 file)
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);

            // local identity-provider accounts + directory profiles
            createUser(localUsers, profiles, "alice", "alice@corp.example", "HR", Roles.EMPLOYEE,
                    hash, now.minus(120, ChronoUnit.DAYS), mapper, encoder, now);
            createUser(localUsers, profiles, "bob", "bob@corp.example", "ENGINEERING", Roles.EMPLOYEE,
                    hash, now.plus(200, ChronoUnit.DAYS), mapper, encoder, now);
            createUser(localUsers, profiles, "carol", "carol@corp.example", "IT-OPS", Roles.ACCESS_MANAGER,
                    hash, now.plus(200, ChronoUnit.DAYS), mapper, encoder, now);
            createUser(localUsers, profiles, "dave", "dave@corp.example", "IT-OPS", Roles.ACCESS_MANAGER,
                    hash, now.plus(200, ChronoUnit.DAYS), mapper, encoder, now);
            createUser(localUsers, profiles, "eve", "eve@corp.example", "COMPLIANCE", Roles.COMPLIANCE_OFFICER,
                    hash, now.plus(200, ChronoUnit.DAYS), mapper, encoder, now);
            createUser(localUsers, profiles, "frank", "frank@corp.example", "COMPLIANCE", Roles.COMPLIANCE_ADMIN,
                    hash, now.plus(200, ChronoUnit.DAYS), mapper, encoder, now);
            createUser(localUsers, profiles, "grace", "grace@corp.example", "AUDIT", Roles.AUDITOR,
                    hash, now.plus(200, ChronoUnit.DAYS), mapper, encoder, now);
            createUser(localUsers, profiles, "integrator", "integrator@corp.example", "PLATFORM", Roles.INTEGRATION,
                    hash, now.plus(200, ChronoUnit.DAYS), mapper, encoder, now);

            // Deliberate violation fixtures:
            // alice: expired certification + standing ADMIN privilege + recert overdue
            grants.save(new Grant(UUID.randomUUID().toString(), aliceId(localUsers), "SYSTEM",
                    "payroll-admin", toJson(mapper, List.of("ADMIN")),
                    now.minus(120, ChronoUnit.DAYS), null, now.minus(30, ChronoUnit.DAYS)));
            // alice: APPROVER + REQUESTER roles together (SoD conflict)
            grants.save(new Grant(UUID.randomUUID().toString(), aliceId(localUsers), "SYSTEM",
                    "payroll-admin", toJson(mapper, List.of("APPROVER", "REQUESTER")),
                    now.minus(60, ChronoUnit.DAYS), null, now.plus(60, ChronoUnit.DAYS)));
            // bob: clean grant + recent activity
            grants.save(new Grant(UUID.randomUUID().toString(), bobId(localUsers), "APPLICATION",
                    "jira-dev", toJson(mapper, List.of("DEVELOPER")),
                    now.minus(10, ChronoUnit.DAYS), null, now.plus(80, ChronoUnit.DAYS)));
            events.save(new AccessEvent(UUID.randomUUID().toString(), bobId(localUsers), "jira-dev",
                    "LOGIN", "10.0.0.5", "demo", "evt-1", now.minus(1, ChronoUnit.HOURS)));
            events.save(new AccessEvent(UUID.randomUUID().toString(), bobId(localUsers), "jira-dev",
                    "USE", "10.0.0.5", "demo", "evt-2", now.minus(30, ChronoUnit.MINUTES)));

            campaigns.save(new RecertCampaign(UUID.randomUUID().toString(), "Q3-2026 Quarterly Access Review",
                    now, now.plus(30, ChronoUnit.DAYS), "system", now));

            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      alice / bob        -> EMPLOYEE
                      carol / dave       -> ACCESS_MANAGER (dual-control pair)
                      eve                -> COMPLIANCE_OFFICER
                      frank              -> COMPLIANCE_ADMIN
                      grace              -> AUDITOR (read-only)
                      integrator         -> INTEGRATION (event ingest)
                    Login: POST /api/v1/auth/token {"username":"grace","password":"..."}
                    ==============================================================""", PASSWORD);
        };
    }

    private static void createUser(LocalUserService service, UserProfileRepository profiles, String username,
                                   String email, String orgUnit, String role, String hash,
                                   Instant certExpiry, ObjectMapper mapper, PasswordEncoder encoder,
                                   Instant now) {
        String id = UUID.randomUUID().toString();
        service.save(new LocalUser(id, username, hash, email, orgUnit, now));
        service.saveRole(id, role);
        profiles.save(new UserProfile(id, username, email, orgUnit, certExpiry, now));
    }

    private static String aliceId(LocalUserService service) {
        return service.findByUsername("alice").map(LocalUser::getId).orElseThrow();
    }

    private static String bobId(LocalUserService service) {
        return service.findByUsername("bob").map(LocalUser::getId).orElseThrow();
    }

    private static String toJson(ObjectMapper mapper, List<String> roles) {
        try {
            return mapper.writeValueAsString(roles);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
