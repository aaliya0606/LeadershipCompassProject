package com.example.leadershipcompass_capstoneprojectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SurveyHistoryEntryResponse {
    private Long resultId;
    private LocalDateTime generateDate;
    private int overallScore;
    private String scoreBand;
    private String summary;
    private int caringTimeScore;
    private int receivingValueScore;
    private int actsOfSupportScore;
    private int wordsOfRecognitionScore;
    private int psychologicalTouchScore;
}