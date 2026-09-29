package com.example.leadershipcompass_capstoneprojectbackend.dto;

public class Feedback360QuestionOptionDTO {

    private Long id;
    private String optionText;
    private Integer displayOrder;

    public Feedback360QuestionOptionDTO() {
    }

    public Feedback360QuestionOptionDTO(Long id, String optionText, Integer displayOrder) {
        this.id = id;
        this.optionText = optionText;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public String getOptionText() {
        return optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}