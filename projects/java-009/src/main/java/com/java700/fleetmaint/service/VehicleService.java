package com.java700.fleetmaint.service;

import com.java700.fleetmaint.common.api.Problems;
import com.java700.fleetmaint.common.audit.AuditLogService;
import com.java700.fleetmaint.domain.OdometerEntry;
import com.java700.fleetmaint.domain.OdometerEntryRepository;
import com.java700.fleetmaint.domain.Vehicle;
import com.java700.fleetmaint.domain.VehicleRepository;
import com.java700.fleetmaint.messaging.DomainEventBus;
import com.java700.fleetmaint.messaging.FleetEvents;
import com.java700.fleetmaint.observability.Metrics;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vehicle registry and odometer ingestion with tamper detection.
 *
 * <p>Readings below the last known odometer are rejected outright (rollback). Readings that
 * exceed the physically plausible daily range are accepted but flagged SUSPICIOUS_JUMP for
 * fleet-manager review.</p>
 */
@Service
public class VehicleService {

    private static final int MAX_DAILY_KM = 1500;

    private final VehicleRepository vehicles;
    private final OdometerEntryRepository odometerEntries;
    private final DomainEventBus events;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;

    public VehicleService(VehicleRepository vehicles, OdometerEntryRepository odometerEntries,
                          DomainEventBus events, AuditLogService audit, Metrics metrics, Clock clock) {
        this.vehicles = vehicles;
        this.odometerEntries = odometerEntries;
        this.events = events;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
    }

    @Transactional
    public Vehicle register(Api.RegisterVehicleRequest request, String username) {
        validate(request);
        if (vehicles.findByPlate(request.plate()).isPresent()) {
            throw new Problems.Conflict("plate " + request.plate() + " is already registered");
        }
        Vehicle vehicle = new Vehicle(UUID.randomUUID().toString(), request.vin().trim(),
                request.plate().trim(), request.make().trim(), request.model().trim(),
                request.modelYear(), request.category(), Vehicle.STATUS_ACTIVE,
                request.initialOdometer(), Instant.now(clock), request.serviceAnchorOdometer(),
                request.lastServiceDate(), trimToNull(request.department()),
                trimToNull(request.driverName()), request.purchaseDate(), Instant.now(clock));
        Vehicle saved = vehicles.save(vehicle);
        audit.record("VEHICLE_REGISTERED", "vehicle", saved.getPlate(),
                "vin=" + saved.getVin() + " by=" + username);
        return saved;
    }

    @Transactional
    public Vehicle setStatus(String vehicleId, String status, String username) {
        Vehicle vehicle = load(vehicleId);
        vehicle.transition(status);
        Vehicle saved = vehicles.save(vehicle);
        audit.record("VEHICLE_STATUS", "vehicle", saved.getPlate(),
                "status=" + status + " by=" + username);
        return saved;
    }

    @Transactional
    public Api.OdometerResult submitOdometer(String vehicleId, int reading, String source,
                                             String username) {
        Vehicle vehicle = load(vehicleId);
        if (reading < 0) {
            throw new Problems.BadRequest("odometer reading cannot be negative");
        }
        if (reading < vehicle.getCurrentOdometer()) {
            audit.record("ODOMETER_ROLLBACK_REJECTED", "vehicle", vehicle.getPlate(),
                    "reading=" + reading + " current=" + vehicle.getCurrentOdometer()
                            + " by=" + username);
            throw new Problems.Conflict("reading " + reading + " is below the last recorded "
                    + vehicle.getCurrentOdometer() + " km — possible rollback");
        }
        boolean suspicious = isSuspiciousJump(vehicle, reading);
        vehicle.applyOdometer(reading, Instant.now(clock));
        vehicles.save(vehicle);
        OdometerEntry entry = new OdometerEntry(UUID.randomUUID().toString(), vehicle.getId(),
                reading, source, suspicious ? OdometerEntry.FLAG_SUSPICIOUS : OdometerEntry.FLAG_OK,
                username, Instant.now(clock));
        odometerEntries.save(entry);
        if (suspicious) {
            metrics.tamperFlag();
            events.publish(new FleetEvents.TamperFlagged(UUID.randomUUID().toString(),
                    Instant.now(clock), vehicle.getId(), vehicle.getPlate(), reading));
            audit.record("ODOMETER_SUSPICIOUS", "vehicle", vehicle.getPlate(),
                    "reading=" + reading + " by=" + username);
        }
        return new Api.OdometerResult(true, entry.getFlag(),
                suspicious ? "accepted but flagged for review: implausible jump"
                           : "accepted",
                reading);
    }

    @Transactional(readOnly = true)
    public Vehicle load(String vehicleId) {
        return vehicles.findById(vehicleId)
                .orElseThrow(() -> new Problems.NotFound("vehicle " + vehicleId));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> all() {
        return vehicles.findAll();
    }

    @Transactional(readOnly = true)
    public List<OdometerEntry> odometerHistory(String vehicleId) {
        return odometerEntries.findByVehicleIdOrderByRecordedAtDesc(vehicleId);
    }

    private boolean isSuspiciousJump(Vehicle vehicle, int reading) {
        Instant last = vehicle.getOdometerUpdatedAt();
        if (last == null) {
            return false;
        }
        long days = Math.max(1, Duration.between(last, Instant.now(clock)).toDays());
        long maxAllowed = Math.max(MAX_DAILY_KM, days * MAX_DAILY_KM * 3L);
        return reading - vehicle.getCurrentOdometer() > maxAllowed;
    }

    private void validate(Api.RegisterVehicleRequest request) {
        if (request.vin() == null || request.vin().isBlank()) {
            throw new Problems.BadRequest("vin is required");
        }
        if (request.plate() == null || request.plate().isBlank()) {
            throw new Problems.BadRequest("plate is required");
        }
        if (request.category() == null || request.category().isBlank()) {
            throw new Problems.BadRequest("category is required");
        }
        if (request.initialOdometer() < 0) {
            throw new Problems.BadRequest("initialOdometer cannot be negative");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
