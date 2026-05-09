package com.example.leadershipcompass_capstoneprojectbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Leadership Compass API")
                        .version("1.0")
                        .description("REST API for Leadership Compass Web Application"));
    }
}