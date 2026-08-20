package com.java700.fleetmaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/** A fleet vehicle with its odometer state and service-history anchors. */
@Entity
@Table(name = "vehicles")
public class Vehicle {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_IN_SHOP = "IN_SHOP";
    public static final String STATUS_COMPLIANCE_HOLD = "COMPLIANCE_HOLD";
    public static final String STATUS_RETIRED = "RETIRED";

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 40, nullable = false, unique = true)
    private String vin;

    @Column(length = 20, nullable = false, unique = true)
    private String plate;

    @Column(length = 60, nullable = false)
    private String make;

    @Column(length = 60, nullable = false)
    private String model;

    @Column(name = "model_year", nullable = false)
    private int modelYear;

    @Column(length = 24, nullable = false)
    private String category;

    @Column(length = 24, nullable = false)
    private String status;

    @Column(name = "current_odometer", nullable = false)
    private int currentOdometer;

    @Column(name = "odometer_updated_at")
    private Instant odometerUpdatedAt;

    @Column(name = "service_anchor_odometer")
    private Integer serviceAnchorOdometer;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(length = 64)
    private String department;

    @Column(name = "driver_name", length = 120)
    private String driverName;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Vehicle() {
    }

    public Vehicle(String id, String vin, String plate, String make, String model, int modelYear,
                   String category, String status, int currentOdometer, Instant odometerUpdatedAt,
                   Integer serviceAnchorOdometer, LocalDate lastServiceDate, String department,
                   String driverName, LocalDate purchaseDate, Instant createdAt) {
        this.id = id;
        this.vin = vin;
        this.plate = plate;
        this.make = make;
        this.model = model;
        this.modelYear = modelYear;
        this.category = category;
        this.status = status;
        this.currentOdometer = currentOdometer;
        this.odometerUpdatedAt = odometerUpdatedAt;
        this.serviceAnchorOdometer = serviceAnchorOdometer;
        this.lastServiceDate = lastServiceDate;
        this.department = department;
        this.driverName = driverName;
        this.purchaseDate = purchaseDate;
        this.createdAt = createdAt;
    }

    public void applyOdometer(int newReading, Instant at) {
        this.currentOdometer = newReading;
        this.odometerUpdatedAt = at;
    }

    public void transition(String newStatus) {
        this.status = newStatus;
    }

    public String getId() {
        return id;
    }

    public String getVin() {
        return vin;
    }

    public String getPlate() {
        return plate;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public int getModelYear() {
        return modelYear;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public int getCurrentOdometer() {
        return currentOdometer;
    }

    public Instant getOdometerUpdatedAt() {
        return odometerUpdatedAt;
    }

    public Integer getServiceAnchorOdometer() {
        return serviceAnchorOdometer;
    }

    public LocalDate getLastServiceDate() {
        return lastServiceDate;
    }

    public String getDepartment() {
        return department;
    }

    public String getDriverName() {
        return driverName;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
