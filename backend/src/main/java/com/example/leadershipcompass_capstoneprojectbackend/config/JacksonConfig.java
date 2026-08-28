package com.example.leadershipcompass_capstoneprojectbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a shared {@link ObjectMapper} for AI/plan JSON prompt building and parsing.
 * <p>
 * Kept separate from {@link AppConfig} so password encoding and other shared app
 * beans remain untouched by the development-plan feature.
 */
@Configuration
public class JacksonConfig {

    /**
     * @return Jackson object mapper used by development-plan services
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
