package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

/**
 * Generates PDF reports containing aggregated Leadership Compass
 * dashboard data for administrators.
 *
 * Reports use the same organisation and department filters as the
 * Admin Dashboard so individual participant results are not exposed.
 */
@Service
@RequiredArgsConstructor
public class AdminPdfService {

    private final AdminDashboardService adminDashboardService;
    private final TemplateEngine templateEngine;

    /**
     * Generates an aggregated Admin Dashboard PDF report for the selected
     * organisation and department.
     *
     * @param organisation organisation to report on, or "all"
     * @param department department to report on, or "all"
     * @return generated PDF as a byte array
     */
    public byte[] generatePdfReport(String organisation, String department) {

        try {
            // Reuse the same aggregated data as the Admin Dashboard.
            AdminDashboardResponse data =
                    adminDashboardService.getDashboardData(organisation, department);

            Context context = new Context();

            context.setVariable("organisation", organisation);
            context.setVariable("department", department);

            context.setVariable("totalUsers", data.getTotalUsers());
            context.setVariable("completedAssessments", data.getCompletedAssessments());
            context.setVariable("completionRate", data.getAssessmentCompletionRate());

            context.setVariable("averageLeadershipScore", data.getAverageLeadershipScore());
            context.setVariable("averageCaringTimeScore", data.getAverageCaringTimeScore());
            context.setVariable("averageReceivingValueScore", data.getAverageReceivingValueScore());
            context.setVariable("averageActsOfSupportScore", data.getAverageActsOfSupportScore());
            context.setVariable("averageWordsOfRecognitionScore", data.getAverageWordsOfRecognitionScore());
            context.setVariable("averagePsychologicalTouchScore", data.getAveragePsychologicalTouchScore());

            context.setVariable("skillGaps", data.getSkillGaps());
            context.setVariable("recommendedFocus", data.getRecommendedFocus());

            String htmlContent =
                    templateEngine.process("admin-report-template", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Admin PDF report", e);
        }
    }
}