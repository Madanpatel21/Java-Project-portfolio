package com.java700.achain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JAVA-006 Audit-Grade Approval and Policy Chain Engine.
 *
 * <p>Audit-grade approvals: policy-version binding, multi-step approval chains with per-step dual control, decision evidence and SLA escalation.</p>
 */
@SpringBootApplication
@EnableScheduling
public class ApprovalChainApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApprovalChainApplication.class, args);
    }
}
