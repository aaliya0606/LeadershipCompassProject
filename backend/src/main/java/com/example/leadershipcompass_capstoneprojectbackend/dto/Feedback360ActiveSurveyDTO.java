package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.time.LocalDateTime;

public class Feedback360ActiveSurveyDTO {

    private boolean hasActiveSurvey;
    private Long id;
    private String token;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private long responseCount;

    public Feedback360ActiveSurveyDTO(
            boolean hasActiveSurvey,
            Long id,
            String token,
            String status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            long responseCount) {

        this.hasActiveSurvey = hasActiveSurvey;
        this.id = id;
        this.token = token;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.responseCount = responseCount;
    }

    public boolean isHasActiveSurvey() {
        return hasActiveSurvey;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public long getResponseCount() {
        return responseCount;
    }
}