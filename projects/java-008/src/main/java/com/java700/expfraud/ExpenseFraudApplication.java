package com.java700.expfraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Expense Fraud & Policy Analytics Engine.
 *
 * <p>Industrial-grade expense fraud detection platform: policy-aware claim scoring, duplicate clustering, weekend-mileage anomaly detection, peer-pattern analytics, four-eyes case workflow, whistleblower intake, and auditor-verifiable evidence packages.</p>
 */
@SpringBootApplication
@EnableScheduling
public class ExpenseFraudApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseFraudApplication.class, args);
    }
}
