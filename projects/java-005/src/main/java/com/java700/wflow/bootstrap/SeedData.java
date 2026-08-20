package com.java700.wflow.bootstrap;

import com.java700.wflow.security.LocalUser;
import com.java700.wflow.security.LocalUserService;
import com.java700.wflow.security.Roles;
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
            if (localUsers.findByUsername("padmin").isPresent()) {
                return;
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);
            createUser(localUsers, "padmin", "padmin@corp.example", Roles.PROCESS_ADMIN, hash, now);
            createUser(localUsers, "operator", "operator@corp.example", Roles.PROCESS_OPERATOR, hash, now);
            createUser(localUsers, "approver", "approver@corp.example", Roles.APPROVER, hash, now);
            createUser(localUsers, "viewer", "viewer@corp.example", Roles.VIEWER, hash, now);
            createUser(localUsers, "admin", "admin@corp.example", Roles.ADMIN, hash, now);
            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      padmin / operator / approver / viewer / admin
                    Try: create a definition (padmin), start an instance
                         (operator), complete tasks (approver).
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
