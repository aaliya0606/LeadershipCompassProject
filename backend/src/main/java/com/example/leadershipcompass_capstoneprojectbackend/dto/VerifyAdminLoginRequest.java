package com.example.leadershipcompass_capstoneprojectbackend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Request body for completing TGG administrator email verification.
 *
 * The challenge token identifies a successful password-authentication
 * attempt, while the code proves access to the administrator's email.
 *
 * Neither value is an authentication JWT.
 */
@Getter
@Setter
public class VerifyAdminLoginRequest {

    /** Temporary challenge token returned after password verification. */
    private String challengeToken;

    /** Six-digit verification code sent to the administrator's email. */
    private String code;
}
