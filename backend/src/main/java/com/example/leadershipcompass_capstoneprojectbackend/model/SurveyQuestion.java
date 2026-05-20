package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_questions")

public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String questionText;

    private String leadershipArea;

    // getters and setters
}
