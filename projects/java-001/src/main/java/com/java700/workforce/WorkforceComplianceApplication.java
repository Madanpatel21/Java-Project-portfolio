package com.java700.workforce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JAVA-001 — Workforce Compliance Evidence Platform.
 *
 * <p>A modular monolith that correlates access grants, approvals, policy versions and access
 * events into a hash-chained, tamper-evident evidence ledger; detects compliance violations;
 * manages recertification campaigns; and produces auditor-grade signed export packages.</p>
 */
@SpringBootApplication
@EnableScheduling
public class WorkforceComplianceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkforceComplianceApplication.class, args);
    }
}
