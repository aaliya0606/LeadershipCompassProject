package com.example.leadershipcompass_capstoneprojectbackend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Survey;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyStatus;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360SurveyRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Feedback360Service {

    private final Feedback360SurveyRepository surveyRepository;
    private final UserRepository userRepository;

    public Feedback360Survey createSurvey(String email) {

        User leader = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Feedback360Survey survey = Feedback360Survey.builder()
                .leader(leader)
                .token(UUID.randomUUID().toString())
                .status(SurveyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        return surveyRepository.save(survey);
    }

    public Feedback360Survey getSurveyByToken(String token) {

        return surveyRepository.findByToken(token)
                .orElseThrow(() ->
                        new RuntimeException(
                                "360 feedback survey not found"));
    }
}