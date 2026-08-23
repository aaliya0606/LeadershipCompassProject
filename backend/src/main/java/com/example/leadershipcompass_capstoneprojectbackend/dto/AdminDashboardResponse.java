package com.example.leadershipcompass_capstoneprojectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalUsers;

    private long completedAssessments;

    private double assessmentCompletionRate;

    private double averageLeadershipScore;

    private double averageCaringTimeScore;

    private double averageReceivingValueScore;

    private double averageActsOfSupportScore;

    private double averageWordsOfRecognitionScore;

    private double averagePsychologicalTouchScore;

    private Map<String, Integer> leadershipProfiles;
}