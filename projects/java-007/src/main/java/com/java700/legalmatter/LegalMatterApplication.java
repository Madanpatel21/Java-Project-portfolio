package com.java700.legalmatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JAVA-007 Legal Matter and Conflict Intelligence.
 *
 * <p>LegalTech: party-graph conflict screening, court-calendar deadline computation and ethical-wall enforcement.</p>
 */
@SpringBootApplication
@EnableScheduling
public class LegalMatterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegalMatterApplication.class, args);
    }
}
