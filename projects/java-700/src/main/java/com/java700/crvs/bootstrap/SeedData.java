package com.java700.crvs.bootstrap;

import com.java700.crvs.offices.OfficeRepository;
import com.java700.crvs.registry.Office;
import com.java700.crvs.security.LocalUser;
import com.java700.crvs.security.LocalUserService;
import com.java700.crvs.security.Roles;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Dev-profile demo dataset: offices and role accounts. */
@Configuration
@Profile("dev")
public class SeedData {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);
    private static final String PASSWORD = "Password123!";

    @Bean
    CommandLineRunner seed(LocalUserService localUsers, OfficeRepository offices,
                           PasswordEncoder encoder, Clock clock) {
        return args -> {
            if (offices.count() > 0) {
                return;
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);

            Office north = offices.save(new Office(UUID.randomUUID().toString(), "RO-NORTH",
                    "Northern District Registry Office", "NORTH"));
            Office south = offices.save(new Office(UUID.randomUUID().toString(), "RO-SOUTH",
                    "Southern District Registry Office", "SOUTH"));

            createUser(localUsers, "registrar", "registrar@registry.gov", Roles.REGISTRAR,
                    north.getId(), hash, now);
            createUser(localUsers, "registrar2", "registrar2@registry.gov", Roles.REGISTRAR,
                    south.getId(), hash, now);
            createUser(localUsers, "supervisor", "supervisor@registry.gov", Roles.SUPERVISOR,
                    null, hash, now);
            createUser(localUsers, "statistician", "statistician@registry.gov", Roles.STATISTICIAN,
                    null, hash, now);
            createUser(localUsers, "verifier", "verifier@bank.example", Roles.VERIFIER_CLIENT,
                    null, hash, now);
            createUser(localUsers, "admin", "admin@registry.gov", Roles.ADMIN, null, hash, now);

            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      registrar (NORTH office) / registrar2 (SOUTH office)
                      supervisor  -> approves (four-eyes)
                      statistician -> read-only vital statistics
                      verifier    -> third-party identity verification
                      admin       -> certificates revocation, dedup adjudication
                    ==============================================================""",
                    PASSWORD);
        };
    }

    private static void createUser(LocalUserService service, String username, String email,
                                   String role, String officeId, String hash, Instant now) {
        String id = UUID.randomUUID().toString();
        service.save(new LocalUser(id, username, hash, email, officeId, now));
        service.saveRole(id, role);
    }
}
