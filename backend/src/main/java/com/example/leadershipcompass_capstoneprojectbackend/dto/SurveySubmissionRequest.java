package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.util.Map;

public class SurveySubmissionRequest {

    private Map<String, Integer> answers;

    public SurveySubmissionRequest() {}

    public SurveySubmissionRequest(Map<String, Integer> answers) {
        this.answers = answers;
    }

    public Map<String, Integer> getAnswers() { return answers; }

    public void setAnswers(Map<String, Integer> answers) { this.answers = answers; }
}