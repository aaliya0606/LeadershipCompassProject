package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private String department;

    @Column
    private String organisation;

    /**
     * When true, plan emails are not sent. Null is treated as still subscribed
     * so existing rows stay opted in when the column is added.
     */
    @Column(name = "email_opt_out")
    private Boolean emailOptOut;

    /** Secret used by the unsubscribe link. Null until the first email is prepared. */
    @Column(name = "unsubscribe_token", unique = true, length = 64)
    private String unsubscribeToken;

    /** SHA-256 hex of the current password-reset token. Null when no reset is pending. */
    @Column(name = "password_reset_token_hash", length = 64)
    private String passwordResetTokenHash;

    /** When the current password-reset token stops working. */
    @Column(name = "password_reset_expires_at")
    private Instant passwordResetExpiresAt;

    /**
     * False until the sign-up email code is confirmed. Null means the account
     * was created before confirmation existed and can still sign in.
     */
    @Column(name = "email_verified")
    private Boolean emailVerified;

    /** SHA-256 hex of the current sign-up code. Null when no code is pending. */
    @Column(name = "signup_code_hash", length = 64)
    private String signupCodeHash;

    /** When the current sign-up code stops working. */
    @Column(name = "signup_code_expires_at")
    private Instant signupCodeExpiresAt;

    /** Failed attempts against the current sign-up code. */
    @Column(name = "signup_code_attempts")
    private Integer signupCodeAttempts;
}
