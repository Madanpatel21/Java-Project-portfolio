package com.java700.wflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JAVA-005 Dynamic Workflow Orchestration Platform.
 *
 * <p>Enterprise workflow orchestration: versioned, model-driven workflows with human tasks, timers, escalation and dynamic routing.</p>
 */
@SpringBootApplication
@EnableScheduling
public class WorkflowOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowOrchestratorApplication.class, args);
    }
}
