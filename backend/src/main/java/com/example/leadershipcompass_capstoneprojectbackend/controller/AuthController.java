package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AuthResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.LoginRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.RegisterRequest;
import com.example.leadershipcompass_capstoneprojectbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for handling authentication-related requests.
 *
 * <p>This controller provides API endpoints for:
 * <ul>
 *     <li>User registration</li>
 *     <li>User login and authentication</li>
 * </ul>
 * </p>
 *
 * <p>The controller communicates with the AuthService layer to process
 * authentication logic and returns JWT authentication responses
 * to the client.</p>
 */


@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Service layer responsible for authentication logic,
     * including registration and login operations.
     */

    private final AuthService authService;

    /**
     * Registers a new user account.
     *
     * <p>This endpoint receives user registration details,
     * sends them to the AuthService for processing,
     * and returns an authentication response containing
     * a JWT token and user details if registration is successful.</p>
     *
     * @param request the registration details submitted by the user
     * @return a ResponseEntity containing the authentication response
     */

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticates an existing user and generates a JWT token.
     *
     * <p>This endpoint receives login credentials,
     * validates the user through the AuthService,
     * and returns an authentication response containing
     * a JWT token if the login is successful.</p>
     *
     * @param request the login credentials submitted by the user
     * @return a ResponseEntity containing the authentication response
     */

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}