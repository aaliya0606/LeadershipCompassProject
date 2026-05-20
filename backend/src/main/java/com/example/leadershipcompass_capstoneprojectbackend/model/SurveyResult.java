package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "survey_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class SurveyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "caring_time_score", nullable = false)
    private int caringTimeScore;

    @Column(name = "receiving_value_score", nullable = false)
    private int receivingValueScore;

    @Column(name = "acts_of_support_score", nullable = false)
    private int actsOfSupportScore;

    @Column(name = "words_of_recognition_score", nullable = false)
    private int wordsOfRecognitionScore;

    @Column(name = "psychological_touch_score", nullable = false)
    private int psychologicalTouchScore;

    @Column(name = "overall_score", nullable = false)
    private int overallScore;

    @Column(name = "score_band", nullable = false, length = 30)
    private String scoreBand;

    @Column(name = "summary", length = 2000)
    private String summary;

    @Column(name = "generate_date", nullable = false, updatable = false)
    private LocalDateTime generateDate;

    @PrePersist
    protected void onPersist() {
        this.generateDate = LocalDateTime.now();
    }

    public void generateResult() {
        this.overallScore = caringTimeScore + receivingValueScore
                + actsOfSupportScore + wordsOfRecognitionScore + psychologicalTouchScore;
        if (overallScore >= 200) {
            this.scoreBand = "High";
        } else if (overallScore >= 150) {
            this.scoreBand = "Strong intent";
        } else if (overallScore >= 100) {
            this.scoreBand = "Needs attention";
        } else {
            this.scoreBand = "Blind spot";
        }
    }

    public String getResult() {
        return String.format(
            "Overall: %d (%s) | CT:%d RV:%d AS:%d WR:%d PT:%d",
            overallScore, scoreBand,
            caringTimeScore, receivingValueScore,
            actsOfSupportScore, wordsOfRecognitionScore,
            psychologicalTouchScore
        );
    }
}
