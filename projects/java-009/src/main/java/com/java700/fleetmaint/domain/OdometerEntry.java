package com.java700.fleetmaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** OdometerEntry. */
@Entity
@Table(name = "odometer_entries")
public class OdometerEntry {

    public static final String FLAG_OK = "OK";
    public static final String FLAG_SUSPICIOUS = "SUSPICIOUS_JUMP";

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "vehicle_id", length = 36, nullable = false)
    private String vehicleId;
    @Column(nullable = false)
    private int reading;
    @Column(length = 16, nullable = false)
    private String source;
    @Column(length = 24)
    private String flag;
    @Column(name = "recorded_by", length = 64, nullable = false)
    private String recordedBy;
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected OdometerEntry() {
    }

    public OdometerEntry(String id, String vehicleId, int reading, String source, String flag, String recordedBy, Instant recordedAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.reading = reading;
        this.source = source;
        this.flag = flag;
        this.recordedBy = recordedBy;
        this.recordedAt = recordedAt;

    }

    public String getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }
    public int getReading() {
        return reading;
    }
    public String getSource() {
        return source;
    }
    public String getFlag() {
        return flag;
    }
    public String getRecordedBy() {
        return recordedBy;
    }
    public Instant getRecordedAt() {
        return recordedAt;
    }
}
