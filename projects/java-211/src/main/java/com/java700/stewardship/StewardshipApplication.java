package com.java700.stewardship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JAVA-211 — Antimicrobial Stewardship Tracker.
 *
 * <p>Hospital-grade antimicrobial stewardship: prescription lifecycle governed by versioned
 * guidelines, pharmacist review tasks and interventions, culture-driven de-escalation and
 * drug-bug alerts, restricted-drug pre-authorization, DOT/DDD utilization metrics and
 * antibiogram aggregation.</p>
 */
@SpringBootApplication
@EnableScheduling
public class StewardshipApplication {

    public static void main(String[] args) {
        SpringApplication.run(StewardshipApplication.class, args);
    }
}
