package com.example.leadershipcompass_capstoneprojectbackend.service;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class InsightGenerator {
    
    private static final String CT = "Caring Time";
    private static final String RV = "Receiving Value";
    private static final String AS = "Acts of Support";
    private static final String WR = "Words of Recognition";
    private static final String PT = "Psychological Touch";

    private static final int STRENGTH_THRESHOLD = 39;
    private static final int WEAKNESS_THRESHOLD = 30;

    public String generateInsights(SurveyResult result){
        StringBuilder sb = new StringBuilder();
        sb.append(generateSummary(result)).append("\n\n");
        sb.append("Stengths: ")
            .append(String.join(",", identifyStrengths(result)))
            .append("\n");
        sb.append("Areas to develop: ")
            .append(String.join(",", identifyWeaknesses(result)))
            .append("\n\n");
            sb.append(generateRecommendations(result));
            return sb.toString();
    }

    public String generateSummary(SurveyResult result){
        Map<String, Integer> scores = categoryScores(result);
        return String.format(
            "Your overall leadership score is %d out of 250 (%s). "
            + "Your strongest leadership langaue is %s, where you demonstrate consistant practice"
            + "The area with the most opportunity for growth is %s",
            result.getOverallScore(),
            result.getScoreBand(),
            highestCategory(scores),
            lowestCategory(scores)

        );
    }

    public List<String> identifyStrengths(SurveyResult result){
        List<String> strengths = new ArrayList<>();
        categoryScores(result).forEach((category, score) -> {
            if (score >= STRENGTH_THRESHOLD) strengths.add(category);
        });
        if (strengths.isEmpty()) {
            strengths.add("No category currently meets the high performance threshold -" +
                "Some adice placeholder");
        }
        return strengths;
    }

     public List<String> identifyWeaknesses(SurveyResult result){
        List<String> weaknesses = new ArrayList<>();
        categoryScores(result).forEach((category, score) -> {
            if (score < WEAKNESS_THRESHOLD) weaknesses.add(category);
        });
        if (weaknesses.isEmpty()) {
            weaknesses.add("No critical weaknesses detected -" +
                "Some advice placeholder.");
        }
        return weaknesses;
    }

    public String generateRecommendations(SurveyResult result){
        StringBuilder sb = new StringBuilder("Recommend next steps:\n");
        categoryScores(result).forEach((category, score) -> {
            if (score < WEAKNESS_THRESHOLD){
                sb.append("-").append(recommendationFor(category, score)).append("\n");
            }
        });
        if (sb.toString().equals("Recommended next steps:\n")){
            sb.append("Review the Action Plans for your lowest-scoring categories and")
            .append("commit to one new habit per week to move from 'Strong intent' to 'High'.\n");
        }
         return sb.toString().trim();
    }

    private Map<String, Integer> categoryScores(SurveyResult result){
        return Map.of(
            CT, result.getCaringTimeScore(),
            RV, result.getReceivingValueScore(),
            AS, result.getActsOfSupportScore(),
            WR, result.getWordsOfRecognitionScore(),
            PT, result.getPsychologicalTouchScore()
        );
    }

    private String highestCategory(Map<String, Integer> scores){
        return scores.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Unknown");
    }


    private String lowestCategory(Map<String, Integer> scores) {
    return scores.entrySet().stream()
        .min(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse("Unknown");
    }

    private String recommendationFor(String category, int score){
        String urgency = score < 20 ? "urgent" : "developing";
        return switch (category){
            case CT -> "(" + urgency +") placeholder advice here";
            case RV -> "(" + urgency +") placeholder advice here";
            case AS -> "(" + urgency +") placeholder advice here";
            case WR -> "(" + urgency +") placeholder advice here";
            case PT -> "(" + urgency +") placeholder advice here";
            default -> "(" + urgency +") placeholder advice here";
            
        };
    }




}
