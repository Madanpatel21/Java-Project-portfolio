package com.java700.achain.bootstrap;

import com.java700.achain.security.LocalUser;
import com.java700.achain.security.LocalUserService;
import com.java700.achain.security.Roles;
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
            if (localUsers.findByUsername("requester").isPresent()) {
                return;
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);
            createUser(localUsers, "requester", "requester@corp.example", Roles.REQUESTER, hash, now);
            createUser(localUsers, "manager", "manager@corp.example", Roles.MANAGER, hash, now);
            createUser(localUsers, "manager2", "manager2@corp.example", Roles.MANAGER, hash, now);
            createUser(localUsers, "director", "director@corp.example", Roles.DIRECTOR, hash, now);
            createUser(localUsers, "legal", "legal@corp.example", Roles.LEGAL_COUNSEL, hash, now);
            createUser(localUsers, "auditor", "auditor@corp.example", Roles.AUDITOR, hash, now);
            createUser(localUsers, "admin", "admin@corp.example", Roles.ADMIN, hash, now);
            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      requester / manager / manager2 / director / legal / auditor / admin
                    Try: create a policy (legal), a chain (admin), a request (requester),
                         approve through the steps (manager, manager2, director...).
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
