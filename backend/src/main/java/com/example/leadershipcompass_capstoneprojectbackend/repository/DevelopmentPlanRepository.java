package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Persistence access for generated {@link DevelopmentPlan} records.
 * <p>
 * Each generate call inserts a new row so older plans remain available.
 * “Current” plan = newest {@code generatedAt} for the user.
 */
public interface DevelopmentPlanRepository extends JpaRepository<DevelopmentPlan, Long> {

    /**
     * Finds the most recently generated plan for a user (current plan).
     *
     * @param userId user id
     * @return latest plan when present
     */
    Optional<DevelopmentPlan> findFirstByUserIdOrderByGeneratedAtDesc(Long userId);

    /**
     * Lists all plan snapshots for a user, newest first.
     *
     * @param userId user id
     * @return plan history
     */
    List<DevelopmentPlan> findByUserIdOrderByGeneratedAtDesc(Long userId);

    /**
     * Loads one plan owned by a specific user.
     *
     * @param id     plan id
     * @param userId owning user id
     * @return plan when found for that user
     */
    Optional<DevelopmentPlan> findByIdAndUserId(Long id, Long userId);

    /**
     * Newest plan for each user. Older snapshots are ignored by the nudge job.
     *
     * @return one current plan per user who has a plan
     */
    @Query("""
            SELECT plan FROM DevelopmentPlan plan
            WHERE plan.generatedAt = (
                SELECT MAX(newer.generatedAt) FROM DevelopmentPlan newer
                WHERE newer.user.id = plan.user.id
            )
            """)
    List<DevelopmentPlan> findLatestPerUser();
}
