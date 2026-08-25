package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private SurveyQuestions question;

    private Integer answerValue;

    // getters and setters
}
