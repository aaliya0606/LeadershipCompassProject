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

                /*
                 * JWT REST API, so CSRF is disabled.
                 */
                .csrf(csrf ->
                        csrf.disable()
                )

                /*
                 * Enable CORS using the configuration below.
                 */
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                /*
                 * JWT authentication is stateless.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth


                        /*
                         * =================================================
                         * CORS PREFLIGHT
                         * =================================================
                         */
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()


                        /*
                         * =================================================
                         * AUTHENTICATION
                         * =================================================
                         */
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()


                        /*
                         * =================================================
                         * DEVELOPMENT / TESTING
                         * =================================================
                         */
                        .requestMatchers(
                                "/h2-console/**",
                                "/api/reports/dummy",
                                "/test-download.html"
                        ).permitAll()


                        /*
                         * =================================================
                         * 360 FEEDBACK - PROTECTED USER ROUTES
                         * =================================================
                         *
                         * IMPORTANT:
                         *
                         * These must appear BEFORE:
                         *
                         * /api/360/surveys/{token}
                         *
                         * otherwise "me" can be interpreted as a token.
                         */


                        /*
                         * Current logged-in user's active survey.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/surveys/me"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )


                        /*
                         * Logged-in user's previous survey history.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/surveys/me/history"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )


                        /*
                         * Logged-in user can view 360 results.
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
                         * 360 FEEDBACK - PUBLIC REVIEWER ROUTES
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
                         * using the unique survey token.
                         *
                         * Example:
                         *
                         * GET /api/360/surveys/abc-123
                         *
                         * Keep this AFTER /me and /me/history.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/360/surveys/{token}"
                        ).permitAll()


                        /*
                         * Reviewer can submit anonymous feedback.
                         *
                         * Example:
                         *
                         * POST /api/360/surveys/abc-123/responses
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/360/surveys/{token}/responses"
                        ).permitAll()


                        /*
                         * =================================================
                         * ADMIN ROUTES
                         * =================================================
                         */
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole(
                                "ADMIN"
                        )

                        .requestMatchers(
                                "/api/dashboard/admin"
                        ).hasRole(
                                "ADMIN"
                        )


                        /*
                         * =================================================
                         * USER DASHBOARD
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
                         * =================================================
                         * EVERYTHING ELSE
                         * =================================================
                         *
                         * Requires authentication.
                         *
                         * This includes:
                         *
                         * POST /api/360/surveys
                         *
                         * so only a logged-in leader can
                         * create a 360 survey.
                         */
                        .anyRequest()
                        .authenticated()
                )


                /*
                 * Required for H2 console during development.
                 */
                .headers(headers ->
                        headers.frameOptions(
                                frame ->
                                        frame.disable()
                        )
                )


                /*
                 * Process JWT before Spring's default
                 * username/password authentication filter.
                 */
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }


    /*
     * =========================================================
     * CORS CONFIGURATION
     * =========================================================
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        /*
         * Frontend development origins.
         *
         * These must be normal URLs,
         * NOT markdown links.
         */
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


        configuration.setAllowCredentials(
                true
        );


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
                "/**",
                configuration
        );


        return source;
    }
}