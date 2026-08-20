package com.java700.fleetmaint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Fleet Maintenance Planning System.
 *
 * <p>Industrial fleet maintenance platform: meter/calendar-based service scheduling (Quartz), work-order lifecycle with parts kitting and reservations, odometer tamper detection, and a compliance inspection ledger with cost analytics per asset.</p>
 */
@SpringBootApplication
@EnableScheduling
public class FleetMaintenanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetMaintenanceApplication.class, args);
    }
}
