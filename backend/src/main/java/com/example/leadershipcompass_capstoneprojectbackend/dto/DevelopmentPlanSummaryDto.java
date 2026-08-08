package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.time.Instant;
import lombok.Data;

/**
 * Lightweight history row for a saved development plan (no week details).
 */
@Data
public class DevelopmentPlanSummaryDto {
    private Long id;
    private Instant generatedAt;
    private String generationSource;
    private Integer caringTimeScore;
    private Integer receivingValueScore;
    private Integer actsOfSupportScore;
    private Integer wordsOfRecognitionScore;
    private Integer psychologicalTouchScore;
}
