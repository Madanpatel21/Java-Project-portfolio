package com.java700.contracts.bootstrap;

import com.java700.contracts.security.LocalUser;
import com.java700.contracts.security.LocalUserService;
import com.java700.contracts.security.Roles;
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

/** Dev-profile demo dataset: role accounts (contracts/obligations are created via the API). */
@Configuration
@Profile("dev")
public class SeedData {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);
    private static final String PASSWORD = "Password123!";

    @Bean
    CommandLineRunner seed(LocalUserService localUsers, PasswordEncoder encoder, Clock clock) {
        return args -> {
            if (localUsers.findByUsername("legal").isPresent()) {
                return;
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);
            createUser(localUsers, "legal", "legal@corp.example", Roles.LEGAL_COUNSEL, hash, now);
            createUser(localUsers, "cmanager", "cmanager@corp.example", Roles.CONTRACT_MANAGER, hash, now);
            createUser(localUsers, "owner", "owner@corp.example", Roles.BUSINESS_OWNER, hash, now);
            createUser(localUsers, "finance", "finance@corp.example", Roles.FINANCE, hash, now);
            createUser(localUsers, "auditor", "auditor@corp.example", Roles.AUDITOR, hash, now);
            createUser(localUsers, "admin", "admin@corp.example", Roles.ADMIN, hash, now);
            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      legal / cmanager / owner / finance / auditor / admin
                    Try: create a contract + version (cmanager), activate with
                         four-eyes (legal + cmanager), attach obligations,
                         run the SLA scan.
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
