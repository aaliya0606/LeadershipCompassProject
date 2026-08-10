package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateDummyReportPdf() {
        try {
            Context context = new Context();
            context.setVariable("name", "Test User");
            context.setVariable("role", "USER");
            context.setVariable("summary", "This is a hardcoded leadership report generated from a Thymeleaf HTML template.");
            context.setVariable("developmentFocus", "Focus on conscious control, care factor, and courage over the next 5 weeks.");

            String htmlContent = templateEngine.process("report-template", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public byte[] generateReportPdf(SurveyResult result) {
        try {
            Context context = new Context();
            context.setVariable("name", result.getUser().getFullName());
            context.setVariable("role", result.getUser().getRole());
            context.setVariable("summary", result.getSummary());
            context.setVariable("overallScore", result.getOverallScore());
            context.setVariable("overallBand", result.getScoreBand());
            context.setVariable("caringTimeScore", result.getCaringTimeScore());
            context.setVariable("receivingValueScore", result.getReceivingValueScore());
            context.setVariable("actsOfSupportScore", result.getActsOfSupportScore());
            context.setVariable("wordsOfRecognitionScore", result.getWordsOfRecognitionScore());
            context.setVariable("psychologicalTouchScore", result.getPsychologicalTouchScore());

            String htmlContent = templateEngine.process("report-template", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}