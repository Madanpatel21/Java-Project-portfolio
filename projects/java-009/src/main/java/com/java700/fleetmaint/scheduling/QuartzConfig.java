package com.java700.fleetmaint.scheduling;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/** Quartz wiring for the due-service forecast job (RAM job store; single node). */
@Configuration
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class QuartzConfig {

    @Value("${app.scheduler.forecast-cron:0 0/30 * * * ?}")
    private String forecastCron;

    @Bean
    JobDetail forecastJobDetail() {
        return JobBuilder.newJob(DueForecastJob.class)
                .withIdentity("dueForecastJob")
                .storeDurably()
                .build();
    }

    @Bean
    Trigger forecastTrigger(JobDetail forecastJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(forecastJobDetail)
                .withIdentity("dueForecastTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(forecastCron))
                .build();
    }

    @Bean
    SpringBeanJobFactory autowiringJobFactory(ApplicationContext context) {
        AutowiringJobFactory factory = new AutowiringJobFactory();
        factory.setApplicationContext(context);
        return factory;
    }

    @Bean
    SchedulerFactoryBean schedulerFactoryBean(JobDetail forecastJobDetail,
                                              Trigger forecastTrigger,
                                              SpringBeanJobFactory jobFactory) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobDetails(forecastJobDetail);
        factory.setTriggers(forecastTrigger);
        factory.setJobFactory(jobFactory);
        factory.setSchedulerName("fleet-scheduler");
        factory.setOverwriteExistingJobs(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);
        return factory;
    }

    /** Autowires @Autowired fields on Quartz-instantiated jobs. */
    static final class AutowiringJobFactory extends SpringBeanJobFactory {

        private AutowireCapableBeanFactory beanFactory;

        @Override
        public void setApplicationContext(ApplicationContext context) {
            this.beanFactory = context.getAutowireCapableBeanFactory();
        }

        @Override
        protected Object createJobInstance(org.quartz.spi.TriggerFiredBundle bundle)
                throws Exception {
            Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job);
            return job;
        }
    }
}
