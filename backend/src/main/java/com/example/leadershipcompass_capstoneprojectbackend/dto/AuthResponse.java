
package com.example.leadershipcompass_capstoneprojectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned by authentication endpoints.
 *
 * Supports regular login, signup email verification,
 * and TGG administrator two-factor authentication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    /** JWT issued after authentication is fully completed. */
    private String token;

    /** Role associated with the authenticated account. */
    private String role;

    /** Message describing the authentication result. */
    private String message;

    /** Shortened JWT preview for compatibility with existing responses. */
    private String tokenPreview;

    /** Whether another verification step is required. */
    private boolean twoFactorRequired;

    /**
     * Temporary challenge token for administrator login verification.
     * This is not a JWT and cannot access protected endpoints.
     */
    private String challengeToken;

    /**
     * Preserves compatibility with existing five-argument constructor calls.
     *
     * @param token JWT, or null when authentication is incomplete
     * @param role account role
     * @param message authentication status message
     * @param tokenPreview abbreviated JWT preview
     * @param twoFactorRequired whether verification is still required
     */
    public AuthResponse(
            String token,
            String role,
            String message,
            String tokenPreview,
            boolean twoFactorRequired) {

        this.token = token;
        this.role = role;
        this.message = message;
        this.tokenPreview = tokenPreview;
        this.twoFactorRequired = twoFactorRequired;
        this.challengeToken = null;
    }
}
