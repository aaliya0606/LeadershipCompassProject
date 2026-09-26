package com.example.leadershipcompass_capstoneprojectbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Answer;

public interface Feedback360AnswerRepository
        extends JpaRepository<Feedback360Answer, Long> {

    List<Feedback360Answer> findByResponseSurveyId(Long surveyId);
}