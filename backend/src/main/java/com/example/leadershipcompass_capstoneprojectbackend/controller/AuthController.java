package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AuthResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.ForgotPasswordRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.LoginRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.MessageResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.RegisterRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.ResetPasswordRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.VerifyLoginCodeRequest;
import com.example.leadershipcompass_capstoneprojectbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller responsible for handling authentication-related requests.
 *
 * <p>This controller provides API endpoints for:
 * <ul>
 *     <li>User registration</li>
 *     <li>User login, email sign-in codes, and password reset</li>
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

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String RESET_ACK =
            "If an account exists for that email, a reset link has been sent.";

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
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(new AuthResponse(null, null, ex.getReason(), null, false));
        }
    }

    /**
     * Authenticates an existing user and returns a JWT.
     * Sign-in does not send an email code. An account that has not confirmed
     * its sign-up email is rejected until that code is entered.
     *
     * @param request the login credentials submitted by the user
     * @return a ResponseEntity containing the authentication response
     */

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Confirms a new account with the email code from sign-up.
     *
     * @param request email and 6-digit code
     * @return confirmation when the code is valid
     */
    @PostMapping("/verify-signup")
    public ResponseEntity<AuthResponse> verifySignupCode(@RequestBody VerifyLoginCodeRequest request) {
        try {
            return ResponseEntity.ok(authService.verifySignupCode(request.getEmail(), request.getCode()));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(new AuthResponse(null, null, ex.getReason(), null, true));
        }
    }

    /**
     * Asks for a password reset email. The response is the same whether or not the account exists.
     *
     * @param request email address
     * @return acknowledgement
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
        } catch (RuntimeException ex) {
            log.warn("Password reset email was not sent.");
        }
        return ResponseEntity.ok(new MessageResponse(RESET_ACK));
    }

    /**
     * Sets a new password from a reset link.
     *
     * @param request token from the email and the new password
     * @return success or a short error
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getPassword());
            return ResponseEntity.ok(new MessageResponse("Password updated. You can sign in with the new password."));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new MessageResponse(ex.getReason()));
        }
    }
}