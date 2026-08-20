package com.java700.fleetmaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** PartReservation. */
@Entity
@Table(name = "part_reservations")
public class PartReservation {

    public static final String STATUS_RESERVED = "RESERVED";
    public static final String STATUS_ISSUED = "ISSUED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public void transition(String newStatus) {
        this.status = newStatus;
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "reservation_no", length = 32, nullable = false, unique = true)
    private String reservationNo;
    @Column(name = "work_order_id", length = 36, nullable = false)
    private String workOrderId;
    @Column(name = "part_code", length = 40, nullable = false)
    private String partCode;
    @Column(nullable = false)
    private int quantity;
    @Column(length = 16, nullable = false)
    private String status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PartReservation() {
    }

    public PartReservation(String id, String reservationNo, String workOrderId, String partCode, int quantity, String status, Instant createdAt) {
        this.id = id;
        this.reservationNo = reservationNo;
        this.workOrderId = workOrderId;
        this.partCode = partCode;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;

    }

    public String getId() {
        return id;
    }

    public String getReservationNo() {
        return reservationNo;
    }
    public String getWorkOrderId() {
        return workOrderId;
    }
    public String getPartCode() {
        return partCode;
    }
    public int getQuantity() {
        return quantity;
    }
    public String getStatus() {
        return status;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
