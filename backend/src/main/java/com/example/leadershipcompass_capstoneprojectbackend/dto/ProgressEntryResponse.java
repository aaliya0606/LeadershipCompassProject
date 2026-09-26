package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProgressEntryResponse {
    private LocalDateTime generateDate;
    private int overallScore;
    private String scoreBand;
    private int caringTimeScore;
    private int receivingValueScore;
    private int actsOfSupportScore;
    private int wordsOfRecognitionScore;
    private int psychologicalTouchScore;
}