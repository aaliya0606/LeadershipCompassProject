package com.example.leadershipcompass_capstoneprojectbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Response;

public interface Feedback360ResponseRepository
        extends JpaRepository<Feedback360Response, Long> {

    long countBySurveyId(Long surveyId);

    
}