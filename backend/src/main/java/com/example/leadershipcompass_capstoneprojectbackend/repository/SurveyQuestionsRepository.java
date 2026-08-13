package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionsRepository extends JpaRepository<SurveyQuestions, Integer> {

    List<SurveyQuestions> findAllByOrderByCategoryAscQuestionIdAsc();

    List<SurveyQuestions> findByCategoryOrderByQuestionIdAsc(String category);
}