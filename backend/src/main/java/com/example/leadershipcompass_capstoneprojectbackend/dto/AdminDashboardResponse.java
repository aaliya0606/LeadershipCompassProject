package com.example.leadershipcompass_capstoneprojectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Response object containing aggregated leadership metrics for the
 * Admin Dashboard.
 *
 * Contains organisation-wide or department-level assessment participation,
 * average leadership scores, identified skill gaps, and recommended
 * focus areas. Individual participant assessment results are not included.
 */

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

    private Map<String, Double> skillGaps;

    private List<String> recommendedFocus;
}