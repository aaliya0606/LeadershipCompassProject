package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

/**
 * Service for generating admin reports in CSV format.
 * */


public class AdminReportService {

    private final AdminDashboardService adminDashboardService;

    public String generateCsvReport(String department) {

        //gets the same AdminDashboardResponse data as the AdminDashboard we already use
        AdminDashboardResponse data =
                adminDashboardService.getDashboardData(department);

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