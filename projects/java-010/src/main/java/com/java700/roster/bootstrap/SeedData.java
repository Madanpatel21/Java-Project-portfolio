package com.java700.roster.bootstrap;

import com.java700.roster.domain.Employee;
import com.java700.roster.domain.EmployeeRepository;
import com.java700.roster.security.LocalUser;
import com.java700.roster.security.LocalUserService;
import com.java700.roster.security.Roles;
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

/** Dev-profile demo dataset: role accounts and a starter workforce. */
@Configuration
@Profile("dev")
public class SeedData {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);
    private static final String PASSWORD = "Password123!";

    @Bean
    CommandLineRunner seed(LocalUserService localUsers, PasswordEncoder encoder, Clock clock,
                           EmployeeRepository employees) {
        return args -> {
            if (localUsers.findByUsername("employee").isPresent()) {
                return;
            }
            Instant now = Instant.now(clock);
            String hash = encoder.encode(PASSWORD);
            String employeeUserId = createUser(localUsers, "employee", Roles.EMPLOYEE, hash, now);
            createUser(localUsers, "manager", Roles.MANAGER, hash, now);
            createUser(localUsers, "admin", Roles.ADMIN, hash, now);
            createUser(localUsers, "auditor", Roles.AUDITOR, hash, now);
            employees.save(new Employee(UUID.randomUUID().toString(), employeeUserId,
                    "EMP-101", "Ana Dias", "OPS", "NURSE,CARE", "FULL_TIME", 40, true, now));
            employees.save(new Employee(UUID.randomUUID().toString(), null,
                    "EMP-102", "Bob Kumar", "OPS", "NURSE", "FULL_TIME", 40, true, now));
            employees.save(new Employee(UUID.randomUUID().toString(), null,
                    "EMP-103", "Carla Menezes", "OPS", "CARE,DRIVER", "FULL_TIME", 40, true, now));
            employees.save(new Employee(UUID.randomUUID().toString(), null,
                    "EMP-104", "Dev Patel", "OPS", "NURSE,DRIVER", "FULL_TIME", 40, true, now));
            employees.save(new Employee(UUID.randomUUID().toString(), null,
                    "EMP-105", "Eva Lind", "OPS", "CARE", "FULL_TIME", 40, true, now));
            log.info("""
                    ==============================================================
                    Dev seed complete. Local users (password: {})
                      employee / manager / admin / auditor
                    Workforce seeded: EMP-101..EMP-105 (OPS department).
                    Try: create a roster from a demand curve (manager), optimize it
                         with Timefold, publish, view your shifts (employee), and
                         request a shift swap.
                    ==============================================================""",
                    PASSWORD);
        };
    }

    private static String createUser(LocalUserService service, String username, String role,
                                     String hash, Instant now) {
        String id = UUID.randomUUID().toString();
        service.save(new LocalUser(id, username, hash, username + "@roster.example", null, now));
        service.saveRole(id, role);
        return id;
    }
}
