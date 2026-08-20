package com.java700.fleetmaint.api;

import com.java700.fleetmaint.domain.OdometerEntry;
import com.java700.fleetmaint.domain.Vehicle;
import com.java700.fleetmaint.security.SecurityUtil;
import com.java700.fleetmaint.service.Api;
import com.java700.fleetmaint.service.VehicleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Vehicle registry, status and odometer ingestion with tamper detection. */
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicles;

    public VehicleController(VehicleService vehicles) {
        this.vehicles = vehicles;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','ADMIN')")
    public Api.VehicleView register(@Valid @RequestBody Api.RegisterVehicleRequest request) {
        return view(vehicles.register(request, SecurityUtil.currentUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DRIVER','FLEET_MANAGER','MECHANIC','PARTS_CLERK',"
            + "'COMPLIANCE_OFFICER','AUDITOR','ADMIN')")
    public List<Api.VehicleView> list() {
        return vehicles.all().stream().map(this::view).toList();
    }

    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasAnyRole('DRIVER','FLEET_MANAGER','MECHANIC','PARTS_CLERK',"
            + "'COMPLIANCE_OFFICER','AUDITOR','ADMIN')")
    public Api.VehicleView get(@PathVariable String vehicleId) {
        return view(vehicles.load(vehicleId));
    }

    @PostMapping("/{vehicleId}/status")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','ADMIN')")
    public Api.VehicleView setStatus(@PathVariable String vehicleId,
                                     @RequestBody StatusRequest request) {
        return view(vehicles.setStatus(vehicleId, request.status(),
                SecurityUtil.currentUsername()));
    }

    @PostMapping("/{vehicleId}/odometer")
    @PreAuthorize("hasAnyRole('DRIVER','MECHANIC','FLEET_MANAGER','ADMIN')")
    public Api.OdometerResult submitOdometer(@PathVariable String vehicleId,
                                             @Valid @RequestBody Api.OdometerRequest request) {
        return vehicles.submitOdometer(vehicleId, request.reading(), request.source(),
                SecurityUtil.currentUsername());
    }

    @GetMapping("/{vehicleId}/odometer")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','AUDITOR','ADMIN')")
    public List<Api.OdometerEntryView> odometerHistory(@PathVariable String vehicleId) {
        return vehicles.odometerHistory(vehicleId).stream().map(this::entryView).toList();
    }

    private Api.VehicleView view(Vehicle vehicle) {
        return new Api.VehicleView(vehicle.getId(), vehicle.getVin(), vehicle.getPlate(),
                vehicle.getMake(), vehicle.getModel(), vehicle.getModelYear(),
                vehicle.getCategory(), vehicle.getStatus(), vehicle.getCurrentOdometer(),
                vehicle.getOdometerUpdatedAt(), vehicle.getDepartment(),
                vehicle.getDriverName());
    }

    private Api.OdometerEntryView entryView(OdometerEntry entry) {
        return new Api.OdometerEntryView(entry.getId(), entry.getVehicleId(), entry.getReading(),
                entry.getSource(), entry.getFlag(), entry.getRecordedBy(), entry.getRecordedAt());
    }

    /** Simple status-change payload. */
    public record StatusRequest(String status) {
    }
}
