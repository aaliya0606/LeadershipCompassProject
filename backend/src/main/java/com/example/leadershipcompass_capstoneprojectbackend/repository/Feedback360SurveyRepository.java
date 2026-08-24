package com.example.leadershipcompass_capstoneprojectbackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Survey;

public interface Feedback360SurveyRepository
        extends JpaRepository<Feedback360Survey, Long> {

    Optional<Feedback360Survey> findByToken(String token);
}