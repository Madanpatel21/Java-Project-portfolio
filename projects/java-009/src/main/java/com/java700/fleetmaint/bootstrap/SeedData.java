package com.java700.fleetmaint.bootstrap;

import com.java700.fleetmaint.security.LocalUser;
import com.java700.fleetmaint.security.LocalUserService;
import com.java700.fleetmaint.security.Roles;
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
            if (localUsers.findByUsername("driver").isPresent()) {
                return;
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);
            createUser(localUsers, "driver", "driver@fleet.example", Roles.DRIVER, hash, now);
            createUser(localUsers, "fleet", "fleet@fleet.example", Roles.FLEET_MANAGER, hash, now);
            createUser(localUsers, "mechanic", "mechanic@fleet.example", Roles.MECHANIC, hash, now);
            createUser(localUsers, "clerk", "clerk@fleet.example", Roles.PARTS_CLERK, hash, now);
            createUser(localUsers, "compliance", "co@fleet.example", Roles.COMPLIANCE_OFFICER,
                    hash, now);
            createUser(localUsers, "auditor", "auditor@fleet.example", Roles.AUDITOR, hash, now);
            createUser(localUsers, "admin", "admin@fleet.example", Roles.ADMIN, hash, now);
            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      driver / fleet / mechanic / clerk / compliance / auditor / admin
                    Try: register a vehicle (fleet), submit odometer readings (driver),
                         run the due-service forecast (fleet), open + complete a work
                         order (mechanic), restock parts (clerk), record inspections
                         (compliance).
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
