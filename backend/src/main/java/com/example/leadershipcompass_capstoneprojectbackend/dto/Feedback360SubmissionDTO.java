package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.util.List;

public class Feedback360SubmissionDTO {

    private List<Feedback360AnswerSubmissionDTO> answers;

    public Feedback360SubmissionDTO() {
    }

    public List<Feedback360AnswerSubmissionDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Feedback360AnswerSubmissionDTO> answers) {
        this.answers = answers;
    }
}