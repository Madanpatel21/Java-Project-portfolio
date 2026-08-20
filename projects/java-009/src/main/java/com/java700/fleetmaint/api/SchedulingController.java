package com.java700.fleetmaint.api;

import com.java700.fleetmaint.security.SecurityUtil;
import com.java700.fleetmaint.service.Api;
import com.java700.fleetmaint.service.ForecastService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Due-service forecasting and the maintenance task board. */
@RestController
@RequestMapping("/api/v1/scheduling")
public class SchedulingController {

    private final ForecastService forecast;

    public SchedulingController(ForecastService forecast) {
        this.forecast = forecast;
    }

    @PostMapping("/forecast/run")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','ADMIN')")
    public Api.ForecastResult runForecast() {
        return forecast.runForecast(SecurityUtil.currentUsername());
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','MECHANIC','AUDITOR','ADMIN')")
    public List<Api.TaskView> tasks() {
        return forecast.dueSoon();
    }
}
