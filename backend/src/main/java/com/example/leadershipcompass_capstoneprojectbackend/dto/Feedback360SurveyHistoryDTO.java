package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.time.LocalDateTime;

public class Feedback360SurveyHistoryDTO {

    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private long responseCount;

    public Feedback360SurveyHistoryDTO(
            Long id,
            String status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            long responseCount) {

        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.responseCount = responseCount;
    }

    public Long getId() {
        return id;
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