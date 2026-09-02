package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.repository.SurveyResultRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final SurveyResultRepository surveyResultRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardData(String department) {

        // Get users
        long totalUsers;

        // Get survey results
        List<SurveyResult> results;

        if (department == null || department.equalsIgnoreCase("all")) {

            totalUsers = userRepository.count();
            results = surveyResultRepository.findAll();

        } else {

            totalUsers = userRepository.findByDepartment(department).size();
            results = surveyResultRepository.findByUserDepartment(department);
        }

        long completedAssessments = results.size();

        // Privacy rule: do not expose aggregated department results
        // when fewer than 6 participants are in the selected group.
        if (department != null
                && !department.equalsIgnoreCase("all")
                && totalUsers < 6) {

        return new AdminDashboardResponse(
                totalUsers,
                completedAssessments,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                Collections.emptyMap()
        );
        }

        // Assessment completion rate
        double completionRate = 0;

        if (totalUsers > 0) {
            completionRate =
                    ((double) completedAssessments / totalUsers) * 100;
        }

        // If there are no completed assessments
        if (results.isEmpty()) {

            return new AdminDashboardResponse(
                    totalUsers,
                    0,
                    completionRate,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    Collections.emptyMap()
            );
        }

        // Calculate averages
        double averageOverall = results.stream()
                .mapToInt(SurveyResult::getOverallScore)
                .average()
                .orElse(0);

        double averageCaringTime = results.stream()
                .mapToInt(SurveyResult::getCaringTimeScore)
                .average()
                .orElse(0);

        double averageReceivingValue = results.stream()
                .mapToInt(SurveyResult::getReceivingValueScore)
                .average()
                .orElse(0);

        double averageActsOfSupport = results.stream()
                .mapToInt(SurveyResult::getActsOfSupportScore)
                .average()
                .orElse(0);

        double averageWordsOfRecognition = results.stream()
                .mapToInt(SurveyResult::getWordsOfRecognitionScore)
                .average()
                .orElse(0);

        double averagePsychologicalTouch = results.stream()
                .mapToInt(SurveyResult::getPsychologicalTouchScore)
                .average()
                .orElse(0);

        return new AdminDashboardResponse(
                totalUsers,
                completedAssessments,
                completionRate,
                averageOverall,
                averageCaringTime,
                averageReceivingValue,
                averageActsOfSupport,
                averageWordsOfRecognition,
                averagePsychologicalTouch,
                Collections.emptyMap()
        );
    }
}