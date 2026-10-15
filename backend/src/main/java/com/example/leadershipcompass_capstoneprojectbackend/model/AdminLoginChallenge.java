
package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

/**
 * Represents a temporary email verification challenge for TGG administrators.
 *
 * After an administrator enters the correct password, the backend creates
 * a challenge and emails a six-digit verification code.
 *
 * A JWT must NOT be issued until this challenge is successfully verified.
 *
 * This entity is separate from signup verification because signup codes
 * and administrator login codes serve different security purposes.
 */
@Entity
@Table(
    name = "admin_login_challenges",
    indexes = {
        @Index(name = "idx_admin_challenge_user", columnList = "user_id")
    }
)
public class AdminLoginChallenge {

    /** Internal database identifier for this challenge. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Administrator who passed the initial password check.
     * The user must have the TGG_ADMIN role.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Hash of an unpredictable challenge token returned to the browser.
     * The raw token must never be stored in the database.
     */
    @Column(name = "challenge_token_hash", nullable = false, unique = true, length = 64)
    private String challengeTokenHash;

    /**
     * Keyed hash of the six-digit email code.
     * The plaintext verification code must never be stored here.
     */
    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    /** Time at which the challenge was created. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Time after which the verification code is no longer valid. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Number of unsuccessful verification attempts. */
    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    /**
     * Set when the challenge is completed or invalidated.
     * A completed challenge must never be reusable.
     */
    @Column(name = "used_at")
    private Instant usedAt;

    /**
     * Prevents concurrent updates from silently overwriting one another.
     * Verification logic must handle optimistic locking failures safely.
     */
    @Version
    private Long version;

    public AdminLoginChallenge() {
        // Required by JPA.
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getChallengeTokenHash() {
        return challengeTokenHash;
    }

    public void setChallengeTokenHash(String challengeTokenHash) {
        this.challengeTokenHash = challengeTokenHash;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    public Long getVersion() {
        return version;
    }
}
