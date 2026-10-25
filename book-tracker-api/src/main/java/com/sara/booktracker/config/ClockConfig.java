package com.sara.booktracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    /** One shared Clock bean. Tests can create the factory with Clock.fixed(...) instead. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}