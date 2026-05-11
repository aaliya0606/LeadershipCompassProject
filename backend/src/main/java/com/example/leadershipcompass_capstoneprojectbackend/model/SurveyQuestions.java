package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class SurveyQuestions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Integer questionId;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "category", nullable = false, length = 10)
    private String category;

    @Column(name = "weight", nullable = false)
    @Builder.Default
    private int weight = 1;

    public SurveyQuestions(String questionText, String category, int weight) {
        this.questionText = questionText;
        this.category = category;
        this.weight = weight;
    }
}