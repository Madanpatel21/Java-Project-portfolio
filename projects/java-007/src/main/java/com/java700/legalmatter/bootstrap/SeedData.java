package com.java700.legalmatter.bootstrap;

import com.java700.legalmatter.security.LocalUser;
import com.java700.legalmatter.security.LocalUserService;
import com.java700.legalmatter.security.Roles;
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

/** Dev-profile demo dataset: role accounts. */
@Configuration
@Profile("dev")
public class SeedData {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);
    private static final String PASSWORD = "Password123!";

    @Bean
    CommandLineRunner seed(LocalUserService localUsers, PasswordEncoder encoder, Clock clock) {
        return args -> {
            if (localUsers.findByUsername("attorney").isPresent()) {
                return;
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);
            createUser(localUsers, "attorney", "attorney@firm.example", Roles.ATTORNEY, hash, now);
            createUser(localUsers, "paralegal", "paralegal@firm.example", Roles.PARALEGAL, hash, now);
            createUser(localUsers, "analyst", "analyst@firm.example", Roles.CONFLICT_ANALYST, hash, now);
            createUser(localUsers, "litteam", "litteam@firm.example", Roles.LITIGATION_TEAM, hash, now);
            createUser(localUsers, "auditor", "auditor@firm.example", Roles.AUDITOR, hash, now);
            createUser(localUsers, "admin", "admin@firm.example", Roles.ADMIN, hash, now);
            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      attorney / paralegal / analyst / litteam / auditor / admin
                    Try: register parties + a matter (attorney), screen a prospective
                         client (analyst), compute court deadlines (paralegal).
                    ==============================================================""",
                    PASSWORD);
        };
    }

    private static void createUser(LocalUserService service, String username, String email,
                                   String role, String hash, Instant now) {
        String id = UUID.randomUUID().toString();
        service.save(new LocalUser(id, username, hash, email, null, now));
        service.saveRole(id, role);
    }
}
