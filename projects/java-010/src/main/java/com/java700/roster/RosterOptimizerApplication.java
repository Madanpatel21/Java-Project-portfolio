package com.java700.roster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Capacity & Shift Rostering Optimizer.
 *
 * <p>Constraint-based workforce rostering with Timefold Solver: demand-curve shift generation, hard labor-law constraints and soft fairness scoring, explainable score breakdowns, self-service availability and manager-approved shift swaps.</p>
 */
@SpringBootApplication
@EnableScheduling
public class RosterOptimizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RosterOptimizerApplication.class, args);
    }
}
