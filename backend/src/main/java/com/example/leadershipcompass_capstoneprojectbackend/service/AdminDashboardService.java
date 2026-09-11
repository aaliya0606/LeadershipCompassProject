package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.repository.SurveyResultRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;

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
 * Dashboard data can be viewed across all users, filtered by organisation,
 * filtered by department, or filtered by a department within an organisation.
 * Results are aggregated to prevent individual participant assessment data
 * from being exposed through the admin dashboard.
 *
 * Department-level aggregate metrics are suppressed when fewer than six
 * participants are present in the selected group.
 */

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final SurveyResultRepository surveyResultRepository;

    /**
    * Retrieves aggregated dashboard data using the existing department-only
    * filtering behaviour.
    *
    * This method is retained for compatibility with existing dashboard,
    * report and test functionality.
    *
    * @param department department to filter by, or "all" for all users
   * @return aggregated Admin Dashboard data
   */    @Transactional(readOnly = true)
   
    public AdminDashboardResponse getDashboardData(String department) {
        return getDashboardData("all", department);
    }

    /**
    * Retrieves aggregated dashboard data for a selected organisation and,
    * where applicable, a department within that organisation.
    *
    * An organisation value of "all" represents users across all organisations.
    * A department value of "all" represents all departments within the selected
    * organisation.
    *
    * @param organisation organisation to filter by, or "all"
    * @param department department to filter by, or "all"
    * @return aggregated Admin Dashboard data
    */

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardData(String organisation, String department) {

        // Get users
        long totalUsers;

        // Get survey results
        List<SurveyResult> results;

        // Retrieve users and survey results based on the selected
        // organisation and department filters.
        if ((organisation == null || organisation.equalsIgnoreCase("all"))
                && (department == null || department.equalsIgnoreCase("all"))) {

        // No filters selected - retrieve all users and results.
        totalUsers = userRepository.count();
        results = surveyResultRepository.findAll();

        } else if (organisation != null
                && !organisation.equalsIgnoreCase("all")
                && (department == null || department.equalsIgnoreCase("all"))) {

        // Organisation selected - include all departments within that organisation.
        totalUsers = userRepository.findByOrganisationIgnoreCase(organisation).size();
        results = surveyResultRepository.findByUserOrganisationIgnoreCase(organisation);

        } else if ((organisation == null || organisation.equalsIgnoreCase("all"))
                && department != null
                && !department.equalsIgnoreCase("all")) {

        // Department-only filtering retained for existing functionality.
        totalUsers = userRepository.findByDepartment(department).size();
        results = surveyResultRepository.findByUserDepartment(department);

        } else {

        // Both organisation and department selected.
        totalUsers =
                userRepository.findByOrganisationIgnoreCaseAndDepartmentIgnoreCase(
                        organisation,
                        department
                ).size();

        results =
                surveyResultRepository
                        .findByUserOrganisationIgnoreCaseAndUserDepartmentIgnoreCase(
                                organisation,
                                department
                        );
        }

        // Count completed assessments by distinct user IDs
        // This ensures that each user is only counted once, even if they have multiple survey results.
        // This is important for calculating the assessment completion rate accurately.

        long completedAssessments = results.stream()
                .map(SurveyResult::getUser)
                .map(User::getId)
                .distinct()
                .count();

                // Assessment completion rate
                double completionRate = 0;

                if (totalUsers > 0) {
                completionRate =
                        ((double) completedAssessments / totalUsers) * 100;
                }

                // If there are no completed assessments, return participation data
                // without generating leadership averages, skill gaps or recommendations.
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