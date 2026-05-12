package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_results")

public class SurveyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer caringTimeScore;

    private Integer receivingValueScore;

    private Integer actsOfSupportScore;

    private Integer wordsOfRecognitionScore;

    private Integer psychologicalTouchScore;

    // getters and setters
}
