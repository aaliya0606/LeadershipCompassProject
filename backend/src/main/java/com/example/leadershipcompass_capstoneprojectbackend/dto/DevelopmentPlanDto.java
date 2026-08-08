package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * API representation of a generated 5-week development plan.
 */
@Data
public class DevelopmentPlanDto {
    private Long id;
    private Instant generatedAt;
    /** {@code AI_BRAIN} or {@code RULE_BASED_FALLBACK}. */
    private String generationSource;
    private Integer caringTimeScore;
    private Integer receivingValueScore;
    private Integer actsOfSupportScore;
    private Integer wordsOfRecognitionScore;
    private Integer psychologicalTouchScore;
    private List<DevelopmentPlanWeekDto> weeks = new ArrayList<>();
}
