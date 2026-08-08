package com.example.leadershipcompass_capstoneprojectbackend.repository;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SurveyResultRepository extends JpaRepository<SurveyResult, Long> {
    Optional<SurveyResult> findFirstByUserIdOrderByIdDesc(Long userId);
}
