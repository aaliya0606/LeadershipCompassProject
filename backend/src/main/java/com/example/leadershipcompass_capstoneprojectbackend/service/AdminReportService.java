package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * Generates CSV reports containing aggregated Leadership Compass
 * assessment metrics for administrators.
 *
 * Reports can be generated organisation-wide or filtered by department.
 * Department-level aggregate metrics are suppressed when fewer than
 * six participants are present to maintain the dashboard's privacy rule.
 */
@Service
@RequiredArgsConstructor


public class AdminReportService {

    private final AdminDashboardService adminDashboardService;

    /**
     * Generates a CSV export using the same aggregated metrics provided
     * by the Admin Dashboard.
     *
     * @param department department to report on, or "all" for organisation-wide data
     * @return aggregated dashboard metrics formatted as CSV
     */

    public String generateCsvReport(String department) {

        //gets the same AdminDashboardResponse data as the AdminDashboard we already use
        AdminDashboardResponse data =
                adminDashboardService.getDashboardData(department);

        // Apply the same minimum-participant privacy rule to department exports.
        boolean suppressed =
        !department.equalsIgnoreCase("all") && data.getTotalUsers() < 6;

        StringBuilder csv = new StringBuilder();

        //Than transform the data into a CSV format, with each metric on a new line
        csv.append("Metric,Value\n");

        csv.append("Department,")
                .append(department)
                .append("\n");

        csv.append("Total Participants,")
                .append(data.getTotalUsers())
                .append("\n");

        csv.append("Completed Assessments,")
                .append(data.getCompletedAssessments())
                .append("\n");

        csv.append("Assessment Completion Rate,")
                .append(suppressed
                    ? "Suppressed (<6 participants)"
                    : String.format("%.2f%%", data.getAssessmentCompletionRate()))
                .append("\n");

        csv.append("Average Leadership Score (out of 250),")
                .append(suppressed
                        ? "Suppressed (<6 participants)"
                        : String.format("%.2f", data.getAverageLeadershipScore()))
                .append("\n");

        csv.append("Average Caring Time Score (out of 50),")
                .append(suppressed
                        ? "Suppressed (<6 participants)"
                        : String.format("%.2f", data.getAverageCaringTimeScore()))
                .append("\n");

        csv.append("Average Receiving Value Score (out of 50),")
                .append(suppressed
                        ? "Suppressed (<6 participants)"
                        : String.format("%.2f", data.getAverageReceivingValueScore()))
                .append("\n");

        csv.append("Average Acts of Support Score (out of 50),")
                .append(suppressed
                        ? "Suppressed (<6 participants)"
                        : String.format("%.2f", data.getAverageActsOfSupportScore()))
                .append("\n");

        csv.append("Average Words of Recognition Score (out of 50),")
                .append(suppressed
                        ? "Suppressed (<6 participants)"
                        : String.format("%.2f", data.getAverageWordsOfRecognitionScore()))
                .append("\n");

        csv.append("Average Psychological Touch Score (out of 50),")
                .append(suppressed
                        ? "Suppressed (<6 participants)"
                        : String.format("%.2f", data.getAveragePsychologicalTouchScore()))
                .append("\n");

        return csv.toString();
    }
}