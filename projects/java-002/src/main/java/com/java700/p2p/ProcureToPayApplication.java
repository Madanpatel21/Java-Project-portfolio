package com.java700.p2p;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JAVA-002 Procure-to-Pay Reconciliation Platform.
 *
 * <p>Enterprise procure-to-pay reconciliation: three-way matching of purchase orders, goods receipts and invoices with tolerance rules, exception routing and posting batches.</p>
 */
@SpringBootApplication
@EnableScheduling
public class ProcureToPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcureToPayApplication.class, args);
    }
}
