package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link SurveyResult} records.
 * <p>
 * Development-plan generation uses {@link #findFirstByUserIdOrderByIdDesc(Long)}
 * to read the learner's latest score snapshot. Survey capture/update remains
 * owned by the survey feature.
 */
public interface SurveyResultRepository extends JpaRepository<SurveyResult, Long> {

    /**
     * Finds the most recently stored survey result for a user.
     *
     * @param userId user id
     * @return latest survey result when present
     */
    Optional<SurveyResult> findFirstByUserIdOrderByIdDesc(Long userId);
}
