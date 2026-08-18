package com.java700.workforce.common.clock;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /** Single injectable time source so domain logic is deterministic under test. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
