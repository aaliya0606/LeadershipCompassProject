package com.example.leadershipcompass_capstoneprojectbackend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Survey;
import com.example.leadershipcompass_capstoneprojectbackend.service.Feedback360Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/360")
@RequiredArgsConstructor
public class Feedback360Controller {

    private final Feedback360Service feedback360Service;

    @PostMapping("/surveys")
    public ResponseEntity<?> createSurvey(
            Authentication authentication) {

        String email = authentication.getName();

        Feedback360Survey survey =
                feedback360Service.createSurvey(email);

        return ResponseEntity.ok(
                Map.of(
                        "id", survey.getId(),
                        "token", survey.getToken(),
                        "status", survey.getStatus()
                )
        );
    }

    @GetMapping("/surveys/{token}")
    public ResponseEntity<?> getSurvey(
            @PathVariable String token) {

        Feedback360Survey survey =
                feedback360Service.getSurveyByToken(token);

        return ResponseEntity.ok(
                Map.of(
                        "id", survey.getId(),
                        "leaderName",
                        survey.getLeader().getFullName(),
                        "status",
                        survey.getStatus()
                )
        );
    }
}