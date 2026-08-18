package com.java700.crvs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JAVA-700 — Digital ID &amp; Civil Registry (CRVS).
 *
 * <p>National civil registration and vital statistics: lifetime identity records, four-eyes
 * life-event registration (birth/marriage/death/correction), a dual hash-chained event ledger
 * (global + per-person), national-ID generation, certificate issuance/verification/revocation,
 * fuzzy duplicate detection, deceased-status propagation and vital-statistics analytics.</p>
 */
@SpringBootApplication
@EnableScheduling
public class CivilRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CivilRegistryApplication.class, args);
    }
}
