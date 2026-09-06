package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.util.List;

public class Feedback360AnswerSubmissionDTO {

    private Long questionId;

    private Integer score;

    private String textResponse;

    private List<Long> selectedOptionIds;

    public Feedback360AnswerSubmissionDTO() {
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getTextResponse() {
        return textResponse;
    }

    public void setTextResponse(String textResponse) {
        this.textResponse = textResponse;
    }

    public List<Long> getSelectedOptionIds() {
        return selectedOptionIds;
    }

    public void setSelectedOptionIds(List<Long> selectedOptionIds) {
        this.selectedOptionIds = selectedOptionIds;
    }
}