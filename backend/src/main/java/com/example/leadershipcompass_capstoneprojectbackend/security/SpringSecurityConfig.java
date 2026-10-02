package com.example.leadershipcompass_capstoneprojectbackend.security;

import jakarta.servlet.DispatcherType;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // JWT REST API, so CSRF is disabled.
                .csrf(csrf -> csrf.disable())

                // Enable CORS using the configuration below.
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // JWT authentication is stateless.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // A missing plan throws 404. Spring then dispatches to
                        // /error; if that dispatch is denied the browser sees 403
                        // and the plan page cannot show its empty state.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()

                        // =================================================
                        // CORS PREFLIGHT
                        // =================================================
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // =================================================
                        // AUTHENTICATION
                        // =================================================
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // One-click and link unsubscribe from plan emails.
                        // No login: the link is opened from the recipient's inbox.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/email/unsubscribe"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/email/unsubscribe"
                        ).permitAll()

                        // =================================================
                        // DEVELOPMENT / TESTING
                        // =================================================
                        .requestMatchers(
                                "/h2-console/**",
                                "/api/reports/dummy",
                                "/test-download.html"
                        ).permitAll()

                        // =================================================
                        // 360 FEEDBACK - PROTECTED USER ROUTES
                        // =================================================

                        // These must appear before the public {token} route
                        // so "me" is not interpreted as a survey token.

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/surveys/me"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/surveys/me/history"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/surveys/{surveyId}/results"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // =================================================
                        // 360 FEEDBACK - PUBLIC REVIEWER ROUTES
                        // =================================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/questions",
                                "/api/360/questions/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/surveys/{token}"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/360/surveys/{token}/responses"
                        ).permitAll()

                        // =================================================
                        // ADMIN
                        // =================================================

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/dashboard/admin"
                        ).hasRole("ADMIN")

                        // =================================================
                        // REPORTS
                        // =================================================

                        .requestMatchers(
                                "/api/reports/**"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // =================================================
                        // LEADERSHIP ASSESSMENT / SURVEY
                        // =================================================

                        .requestMatchers(
                                "/api/survey/questions",
                                "/api/survey/submit",
                                "/api/survey/history"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        .requestMatchers(
                                "/api/dashboard/user",
                                "/api/dashboard/suggested-modules",
                                "/api/dashboard/peer-comparison",
                                "/api/dashboard/latest-scores"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // USER PROFILE
                        .requestMatchers(
                                "/api/profile"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        .requestMatchers(
                                "/api/survey/admin/**"
                        ).hasRole("ADMIN")

                        // =================================================
                        // USER DASHBOARD
                        // =================================================

                        .requestMatchers(
                                "/api/dashboard/user",
                                "/api/dashboard/suggested-modules",
                                "/api/dashboard/peer-comparison",
                                "/api/dashboard/latest-scores"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // =================================================
                        // DEVELOPMENT PLANS
                        // =================================================

                        .requestMatchers(
                                "/api/development-plans/**"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        .requestMatchers(
                                "/api/email/subscription"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // =================================================
                        // AI-BRAIN CHAT
                        // =================================================

                        .requestMatchers(
                                "/api/ai-brain/**"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // =================================================
                        // RESOURCES
                        // =================================================

                        // Users and admins may view resources.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/resources/**"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // Resource management is admin-only.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/resources/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/resources/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/resources/**"
                        ).hasRole("ADMIN")

                        // =================================================
                        // SWAGGER
                        // =================================================

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // =================================================
                        // EVERYTHING ELSE
                        // =================================================

                        // Requires authentication.
                        //
                        // This includes POST /api/360/surveys,
                        // meaning only a logged-in leader can create
                        // a 360 survey.
                        .anyRequest()
                        .authenticated()
                )

                // Required for H2 console during development.
                .headers(headers ->
                        headers.frameOptions(
                                frame -> frame.disable()
                        )
                )

                // Process JWT before Spring's default authentication filter.
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // =========================================================
    // CORS CONFIGURATION
    // =================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:3000",
                        "http://127.0.0.1:5500",
                        "http://localhost:5500",
                        "http://127.0.0.1:5501",
                        "http://localhost:5501",
                        "http://localhost:5173",
                        "https://delightful-forest-04c15c700.5.azurestaticapps.net"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}