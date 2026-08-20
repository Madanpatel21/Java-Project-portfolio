package com.java700.p2p.bootstrap;

import com.java700.p2p.security.LocalUser;
import com.java700.p2p.security.LocalUserService;
import com.java700.p2p.security.Roles;
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

/** Dev-profile demo dataset: role accounts (POs/GRs/invoices are created via the API). */
@Configuration
@Profile("dev")
public class SeedData {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);
    private static final String PASSWORD = "Password123!";

    @Bean
    CommandLineRunner seed(LocalUserService localUsers, PasswordEncoder encoder, Clock clock) {
        return args -> {
            if (localUsers.findByUsername("apclerk").isPresent()) {
                return;
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);
            createUser(localUsers, "apclerk", "apclerk@corp.example", Roles.AP_CLERK, hash, now);
            createUser(localUsers, "apmanager", "apmanager@corp.example", Roles.AP_MANAGER, hash, now);
            createUser(localUsers, "procurement", "procurement@corp.example", Roles.PROCUREMENT, hash, now);
            createUser(localUsers, "auditor", "auditor@corp.example", Roles.AUDITOR, hash, now);
            createUser(localUsers, "admin", "admin@corp.example", Roles.ADMIN, hash, now);
            createUser(localUsers, "integrator", "integrator@corp.example", Roles.INTEGRATION, hash, now);
            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      apclerk / apmanager / procurement / auditor / admin / integrator
                    Try: create a PO (procurement), post a GR (procurement),
                         ingest an invoice (apclerk), waive exceptions (apmanager),
                         run the batch (apmanager).
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
