package com.java700.fleetmaint.scheduling;

import com.java700.fleetmaint.service.Api;
import com.java700.fleetmaint.service.ForecastService;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/** Quartz job: periodic fleet-wide due-service forecast. */
public class DueForecastJob extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(DueForecastJob.class);

    @Autowired
    private ForecastService forecast;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            Api.ForecastResult result = forecast.runForecast("quartz-scheduler");
            log.info("Scheduled forecast pass: {} created, {} updated, {} overdue",
                    result.created(), result.updated(), result.overdue());
        } catch (RuntimeException ex) {
            log.error("Scheduled forecast pass failed", ex);
        }
    }
}
