package com.java700.expfraud.bootstrap;

import com.java700.expfraud.security.LocalUser;
import com.java700.expfraud.security.LocalUserService;
import com.java700.expfraud.security.Roles;
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

/** Dev-profile demo dataset: role accounts for the live demo. */
@Configuration
@Profile("dev")
public class SeedData {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);
    private static final String PASSWORD = "Password123!";

    @Bean
    CommandLineRunner seed(LocalUserService localUsers, PasswordEncoder encoder, Clock clock) {
        return args -> {
            if (localUsers.findByUsername("employee").isPresent()) {
                return;
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);
            createUser(localUsers, "employee", "employee@corp.example", Roles.EMPLOYEE, hash, now);
            createUser(localUsers, "manager", "manager@corp.example", Roles.MANAGER, hash, now);
            createUser(localUsers, "investigator", "inv1@corp.example", Roles.FRAUD_INVESTIGATOR,
                    hash, now);
            createUser(localUsers, "investigator2", "inv2@corp.example", Roles.FRAUD_INVESTIGATOR,
                    hash, now);
            createUser(localUsers, "auditor", "auditor@corp.example", Roles.AUDITOR, hash, now);
            createUser(localUsers, "admin", "admin@corp.example", Roles.ADMIN, hash, now);
            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      employee / manager / investigator / investigator2 / auditor / admin
                    Try: submit a claim (employee), approve it (manager), watch a
                         weekend-mileage or duplicate claim open a fraud case, run the
                         four-eyes workflow (investigator + investigator2), file an
                         anonymous tip (POST /api/v1/tips, no auth).
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
