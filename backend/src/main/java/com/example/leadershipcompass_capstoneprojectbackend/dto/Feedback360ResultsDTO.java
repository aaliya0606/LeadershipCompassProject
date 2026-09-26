package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.util.List;
import java.util.Map;

public class Feedback360ResultsDTO {

    private long responseCount;

    private List<Feedback360RatingResultDTO> ratings;

    private Map<Integer, List<Feedback360OptionResultDTO>> optionResults;

    private Map<Integer, List<String>> writtenFeedback;

    public Feedback360ResultsDTO(
            long responseCount,
            List<Feedback360RatingResultDTO> ratings,
            Map<Integer, List<Feedback360OptionResultDTO>> optionResults,
            Map<Integer, List<String>> writtenFeedback) {

        this.responseCount = responseCount;
        this.ratings = ratings;
        this.optionResults = optionResults;
        this.writtenFeedback = writtenFeedback;
    }

    public long getResponseCount() {
        return responseCount;
    }

    public List<Feedback360RatingResultDTO> getRatings() {
        return ratings;
    }

    public Map<Integer, List<Feedback360OptionResultDTO>> getOptionResults() {
        return optionResults;
    }

    public Map<Integer, List<String>> getWrittenFeedback() {
        return writtenFeedback;
    }
}