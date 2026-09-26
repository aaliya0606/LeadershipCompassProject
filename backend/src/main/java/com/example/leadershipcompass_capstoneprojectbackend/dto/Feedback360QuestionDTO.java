package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.util.List;

public class Feedback360QuestionDTO {

    private Long id;
    private Integer questionNumber;
    private String questionText;
    private String questionType;
    private String category;
    private List<Feedback360QuestionOptionDTO> options;

    public Feedback360QuestionDTO(
            Long id,
            Integer questionNumber,
            String questionText,
            String questionType,
            String category,
            List<Feedback360QuestionOptionDTO> options) {

        this.id = id;
        this.questionNumber = questionNumber;
        this.questionText = questionText;
        this.questionType = questionType;
        this.category = category;
        this.options = options;
    }

    public Long getId() {
        return id;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public String getCategory() {
        return category;
    }

    public List<Feedback360QuestionOptionDTO> getOptions() {
        return options;
    }
}