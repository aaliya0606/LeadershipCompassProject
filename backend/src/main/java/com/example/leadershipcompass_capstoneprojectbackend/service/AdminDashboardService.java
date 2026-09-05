package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.repository.SurveyResultRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides aggregated leadership assessment data for the Admin Dashboard.
 *
 * Dashboard data can be viewed organisation-wide or filtered by department.
 * Results are aggregated to prevent individual participant assessment data
 * from being exposed through the admin dashboard.
 *
 * Department-level aggregate metrics are suppressed when fewer than six
 * participants are present in the selected department.
 */

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final SurveyResultRepository surveyResultRepository;

    /**
     * Retrieves aggregated dashboard metrics for the organisation or a
     * selected department.
     * The response includes assessment participation, average leadership
     * scores, the three lowest-scoring leadership areas, and recommended
     * focus areas.
     * @param department department to filter by, or "all" for organisation-wide data
     *  @return aggregated Admin Dashboard data
     */

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
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        Collections.emptyList()
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
                        Collections.emptyMap(),
                                Collections.emptyMap(),
                                Collections.emptyList()
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

                // Identify the three lowest-scoring leadership areas as skill gaps
                Map<String, Double> allLeadershipAreas = new LinkedHashMap<>();

                allLeadershipAreas.put("Caring Time", averageCaringTime);
                allLeadershipAreas.put("Receiving Value", averageReceivingValue);
                allLeadershipAreas.put("Acts of Support", averageActsOfSupport);
                allLeadershipAreas.put("Words of Recognition", averageWordsOfRecognition);
                allLeadershipAreas.put("Psychological Touch", averagePsychologicalTouch);

                Map<String, Double> skillGaps = new LinkedHashMap<>();

                allLeadershipAreas.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .limit(3)
                        .forEach(entry ->
                                skillGaps.put(entry.getKey(), entry.getValue())
                        );
                List<String> recommendedFocus = skillGaps.keySet().stream()
                        .map(this::recommendationForSkillGap)
                        .toList();

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
                        Collections.emptyMap(),
                        skillGaps,
                        recommendedFocus
                );
        }

     /**
     *  Maps an identified leadership skill gap to an actionable
     * recommended focus area for administrators.
     * */

    private String recommendationForSkillGap(String category) {
        return switch (category) {
                case "Caring Time" ->
                        "Prioritise regular one-on-one leadership activities and protected time with team members.";
                case "Receiving Value" ->
                        "Strengthen active listening practices and follow-up on team feedback.";
                case "Acts of Support" ->
                        "Focus on practical support behaviours and removing barriers that affect team performance.";
                case "Words of Recognition" ->
                        "Prioritise timely and specific recognition of team contributions.";
                case "Psychological Touch" ->
                        "Strengthen psychological safety and regular wellbeing check-ins across teams.";
                default ->
                        "Review learning resources related to this leadership area.";
        };
        }


}