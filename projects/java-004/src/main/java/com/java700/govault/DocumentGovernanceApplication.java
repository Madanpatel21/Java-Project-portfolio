package com.java700.govault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JAVA-004 Enterprise Document Governance Vault.
 *
 * <p>Enterprise document governance: classification, retention schedules, legal holds, disposition with proof and quarantine workflow.</p>
 */
@SpringBootApplication
@EnableScheduling
public class DocumentGovernanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentGovernanceApplication.class, args);
    }
}
