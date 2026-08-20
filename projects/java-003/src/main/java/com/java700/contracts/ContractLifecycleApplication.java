package com.java700.contracts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JAVA-003 Contract Lifecycle & Obligation Engine.
 *
 * <p>Enterprise contract lifecycle: obligation extraction into schedules with rules, reminders, approvals, state machines and expiry handling.</p>
 */
@SpringBootApplication
@EnableScheduling
public class ContractLifecycleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractLifecycleApplication.class, args);
    }
}
