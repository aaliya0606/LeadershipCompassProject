package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.model.Resource;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

@Service
public class PdfService {

    private final TemplateEngine templateEngine;
    private final SurveyService surveyService; // NEW: needed to reuse band() classification logic
    private final InsightGenerator insightGenerator; // NEW: needed to reuse generateRecommendations()
    private static final String LOGO_BASE64 = "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";

    // CHANGED: constructor now also takes SurveyService and InsightGenerator
    public PdfService(TemplateEngine templateEngine, SurveyService surveyService, InsightGenerator insightGenerator) {
        this.templateEngine = templateEngine;
        this.surveyService = surveyService;
        this.insightGenerator = insightGenerator;
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

            // CHANGED: trimmed out "Recommended next steps" section from summary for PDF display,
            // since it's now shown separately in the Suggested Learning Path section
            String fullSummary = result.getSummary();
            String trimmedSummary = fullSummary.split("Recommended next steps:")[0].trim();
            context.setVariable("summary", trimmedSummary);

            context.setVariable("overallScore", result.getOverallScore());
            context.setVariable("overallBand", result.getScoreBand());
            context.setVariable("caringTimeScore", result.getCaringTimeScore());
            context.setVariable("receivingValueScore", result.getReceivingValueScore());
            context.setVariable("actsOfSupportScore", result.getActsOfSupportScore());
            context.setVariable("wordsOfRecognitionScore", result.getWordsOfRecognitionScore());
            context.setVariable("psychologicalTouchScore", result.getPsychologicalTouchScore());

            // NEW: reuse existing band() logic from SurveyService for each category's tag
            context.setVariable("caringTimeBand", surveyService.band(result.getCaringTimeScore()));
            context.setVariable("receivingValueBand", surveyService.band(result.getReceivingValueScore()));
            context.setVariable("actsOfSupportBand", surveyService.band(result.getActsOfSupportScore()));
            context.setVariable("wordsOfRecognitionBand", surveyService.band(result.getWordsOfRecognitionScore()));
            context.setVariable("psychologicalTouchBand", surveyService.band(result.getPsychologicalTouchScore()));

            // NEW: per-category feedback messages, reused from SurveyService.message()
            context.setVariable("caringTimeMessage", surveyService.message("Caring Time", result.getCaringTimeScore()));
            context.setVariable("receivingValueMessage", surveyService.message("Receiving Value", result.getReceivingValueScore()));
            context.setVariable("actsOfSupportMessage", surveyService.message("Acts of Support", result.getActsOfSupportScore()));
            context.setVariable("wordsOfRecognitionMessage", surveyService.message("Words of Recognition", result.getWordsOfRecognitionScore()));
            context.setVariable("psychologicalTouchMessage", surveyService.message("Psychological Touch", result.getPsychologicalTouchScore()));

            // NEW: learning path, reused from InsightGenerator's existing recommendation logic
            context.setVariable("learningPath", insightGenerator.generateRecommendations(result));

            // NEW: suggested resources from the Resource Library, matched to weakest categories
            List<Resource> suggestedResources = surveyService.getSuggestedLearningPath(result);
            context.setVariable("suggestedResources", suggestedResources);

            // NEW: Call new method: generateChartImage to generate the radar chart from real scores and pass it in as base64
            String chartImage = generateChartImage(
                    result.getCaringTimeScore(),
                    result.getReceivingValueScore(),
                    result.getActsOfSupportScore(),
                    result.getWordsOfRecognitionScore(),
                    result.getPsychologicalTouchScore()
            );
            context.setVariable("chartImage", chartImage);
            // NEW: passes the IP logo into the template
            context.setVariable("logoImage", LOGO_BASE64);

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

    // NEW: builds the radar chart and returns it as a base64 PNG string
    private String generateChartImage(int caringTime, int receivingValue, int actsOfSupport, int wordsOfRecognition, int psychologicalTouch) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(caringTime, "Scores", "Caring Time");
            dataset.addValue(receivingValue, "Scores", "Receiving Value");
            dataset.addValue(actsOfSupport, "Scores", "Acts of Support");
            dataset.addValue(wordsOfRecognition, "Scores", "Words of Recognition");
            dataset.addValue(psychologicalTouch, "Scores", "Psychological Touch");

            SpiderWebPlot plot = new SpiderWebPlot(dataset);

            // NEW: match Kari's colours — navy line/points
            Color navy = new Color(0, 40, 75);
            plot.setSeriesPaint(0, navy);
            plot.setSeriesOutlineStroke(0, new BasicStroke(2f));

            // NEW: more breathing room around the edge so labels like "Psychological Touch" aren't cut off
            plot.setInteriorGap(0.35);
            plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));

            // NEW: removes the plot's grey outline box, matching Kari's borderless chart
            plot.setOutlineVisible(false);

            JFreeChart chart = new JFreeChart(plot);
            chart.removeLegend();               // NEW: removes the default "● Scores" legend box because already in html file
            chart.setBorderVisible(false);      // NEW: safety net: removes chart-level border too
            chart.setBackgroundPaint(Color.WHITE);

            // CHANGED: 500 -> 550, extra canvas room to fit the wider label gap above
            BufferedImage image = chart.createBufferedImage(700, 600);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeBufferedImageAsPNG(baos, image);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate chart image", e);
        }
    }
}
