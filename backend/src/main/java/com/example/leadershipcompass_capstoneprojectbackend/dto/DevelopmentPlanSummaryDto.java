package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.time.Instant;
import lombok.Data;

/**
 * Lightweight history row for a saved development plan (no week details).
 * <p>
 * Used by {@code GET /api/development-plans} so clients can list past plans
 * without loading every week's content.
 */
@Data
public class DevelopmentPlanSummaryDto {

    /** Persisted plan id. */
    private Long id;

    /** When this plan snapshot was created. */
    private Instant generatedAt;

    /** {@code AI_BRAIN} or {@code RULE_BASED_FALLBACK}. */
    private String generationSource;

    /** Caring Time score snapshot (0–50). */
    private Integer caringTimeScore;

    /** Receiving Value score snapshot (0–50). */
    private Integer receivingValueScore;

    /** Acts of Support score snapshot (0–50). */
    private Integer actsOfSupportScore;

    /** Words of Recognition score snapshot (0–50). */
    private Integer wordsOfRecognitionScore;

    /** Psychological Touch score snapshot (0–50). */
    private Integer psychologicalTouchScore;
}
