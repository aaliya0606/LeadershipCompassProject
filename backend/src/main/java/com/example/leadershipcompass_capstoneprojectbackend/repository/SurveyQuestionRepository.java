package com.example.leadershipcompass_capstoneprojectbackend.repository;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
}