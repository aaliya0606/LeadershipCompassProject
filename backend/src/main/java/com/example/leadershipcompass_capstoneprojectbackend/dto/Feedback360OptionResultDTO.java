package com.example.leadershipcompass_capstoneprojectbackend.dto;

public class Feedback360OptionResultDTO {

    private String optionText;
    private long count;

    public Feedback360OptionResultDTO(
            String optionText,
            long count) {

        this.optionText = optionText;
        this.count = count;
    }

    public String getOptionText() {
        return optionText;
    }

    public long getCount() {
        return count;
    }
}