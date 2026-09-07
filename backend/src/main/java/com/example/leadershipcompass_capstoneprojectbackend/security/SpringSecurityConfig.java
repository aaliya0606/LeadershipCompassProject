package com.example.leadershipcompass_capstoneprojectbackend.security;

import java.util.List;

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

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                .csrf(csrf -> csrf.disable())

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Allow browser CORS preflight requests.
                         */
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()


                        /*
                         * Authentication endpoints.
                         */
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()


                        /*
                         * Development/testing endpoints.
                         */
                        .requestMatchers(
                                "/h2-console/**",
                                "/api/reports/dummy",
                                "/test-download.html"
                        ).permitAll()


                        /*
                         * =================================================
                         * 360 FEEDBACK - PUBLIC REVIEWER ENDPOINTS
                         * =================================================
                         */

                        /*
                         * Reviewer can retrieve survey questions.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/questions",
                                "/api/360/questions/**"
                        ).permitAll()


                        /*
                         * Reviewer can load survey information
                         * using the unique token.
                         *
                         * Example:
                         * GET /api/360/surveys/abc-123
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/surveys/{token}"
                        ).permitAll()


                        /*
                         * Reviewer can anonymously submit feedback.
                         *
                         * Example:
                         * POST /api/360/surveys/abc-123/responses
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/360/surveys/{token}/responses"
                        ).permitAll()


                        /*
                         * =================================================
                         * 360 FEEDBACK - PROTECTED RESULTS
                         * =================================================
                         *
                         * Only logged-in USER or ADMIN accounts
                         * can retrieve survey results.
                         *
                         * Example:
                         * GET /api/360/surveys/16/results
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/surveys/{surveyId}/results"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )


                        /*
                         * =================================================
                         * ADMIN ROUTES
                         * =================================================
                         */

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/dashboard/admin"
                        ).hasRole("ADMIN")


                        /*
                         * =================================================
                         * USER ROUTES
                         * =================================================
                         */

                        .requestMatchers(
                                "/api/dashboard/user"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )


                        /*
                         * =================================================
                         * SWAGGER
                         * =================================================
                         */

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()


                        /*
                         * Everything else requires authentication.
                         *
                         * This means:
                         *
                         * POST /api/360/surveys
                         *
                         * remains protected, so only a logged-in
                         * leader can create their 360 survey.
                         */
                        .anyRequest()
                        .authenticated()
                )


                /*
                 * Allow H2 console frames during development.
                 */
                .headers(headers ->
                        headers.frameOptions(
                                frame -> frame.disable()
                        )
                )


                /*
                 * Process JWT before Spring's default
                 * authentication filter.
                 */
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:3000",
                        "http://127.0.0.1:5500",
                        "http://localhost:5500",
                        "http://localhost:5173"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
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