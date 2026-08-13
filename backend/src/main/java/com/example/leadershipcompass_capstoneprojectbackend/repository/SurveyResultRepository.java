package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResultRepository extends JpaRepository<SurveyResult, Long> {

    List<SurveyResult> findByUserOrderByGenerateDateDesc(User user);

    /** Latest saved survey result for a user; used by development-plan generation. */
    Optional<SurveyResult> findFirstByUserOrderByGenerateDateDesc(User user);
}
