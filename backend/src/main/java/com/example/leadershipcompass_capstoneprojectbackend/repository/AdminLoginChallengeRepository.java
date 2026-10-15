
package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.AdminLoginChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Database access for temporary TGG administrator login challenges.
 *
 * Each challenge is created after successful password authentication.
 * The verification service uses this repository to retrieve and
 * validate the challenge before issuing a JWT.
 */
public interface AdminLoginChallengeRepository
        extends JpaRepository<AdminLoginChallenge, Long> {

    /**
     * Finds a challenge using the SHA-256 hash of its random token.
     *
     * The browser receives the original random token, while the
     * database stores only its hash.
     *
     * @param challengeTokenHash hash of the submitted challenge token
     * @return the matching challenge, if one exists
     */
    Optional<AdminLoginChallenge> findByChallengeTokenHash(
            String challengeTokenHash
    );

    /**
     * Retrieves a login challenge with an exclusive database lock.
     *
     * Prevents simultaneous verification requests from consuming
     * the same administrator login challenge.
     *
     * Must be called within an active transaction.
     *
     * @param challengeTokenHash SHA-256 hash of the challenge token
     * @return matching challenge, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c
        FROM AdminLoginChallenge c
        WHERE c.challengeTokenHash = :challengeTokenHash
        """)
    Optional<AdminLoginChallenge> findWithLockByChallengeTokenHash(
            @org.springframework.data.repository.query.Param("challengeTokenHash")
            String challengeTokenHash
    );
    /**
     * Counts recent login challenges for an administrator.
     *
     * Used to prevent repeated requests from generating
     * unlimited verification emails.
     *
     * @param userId administrator's database ID
     * @param createdAt earliest creation time to include
     * @return number of recent challenges
     */
    long countByUserIdAndCreatedAtAfter(
            Long userId,
            java.time.Instant createdAt
    );

}
