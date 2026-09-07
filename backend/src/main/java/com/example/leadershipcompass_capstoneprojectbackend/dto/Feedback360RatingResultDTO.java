package com.example.leadershipcompass_capstoneprojectbackend.dto;

public class Feedback360RatingResultDTO {

    private Integer questionNumber;
    private String category;
    private double averageScore;

    public Feedback360RatingResultDTO(
            Integer questionNumber,
            String category,
            double averageScore) {

        this.questionNumber = questionNumber;
        this.category = category;
        this.averageScore = averageScore;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public String getCategory() {
        return category;
    }

    public double getAverageScore() {
        return averageScore;
    }
}