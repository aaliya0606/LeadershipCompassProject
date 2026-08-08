package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * API representation of a generated 5-week development plan.
 * <p>
 * Returned by current/history-detail/generate endpoints, and by the
 * non-persisted POC preview endpoint (where {@link #id} may be {@code null}).
 */
@Data
public class DevelopmentPlanDto {

    /** Persisted plan id, or {@code null} for preview-only responses. */
    private Long id;

    /** When this plan snapshot was created. */
    private Instant generatedAt;

    /** {@code AI_BRAIN} when AI contributed weeks; otherwise {@code RULE_BASED_FALLBACK}. */
    private String generationSource;

    /** Caring Time score snapshot used when the plan was built (0–50). */
    private Integer caringTimeScore;

    /** Receiving Value score snapshot used when the plan was built (0–50). */
    private Integer receivingValueScore;

    /** Acts of Support score snapshot used when the plan was built (0–50). */
    private Integer actsOfSupportScore;

    /** Words of Recognition score snapshot used when the plan was built (0–50). */
    private Integer wordsOfRecognitionScore;

    /** Psychological Touch score snapshot used when the plan was built (0–50). */
    private Integer psychologicalTouchScore;

    /** Ordered weekly recommendations (typically five weeks). */
    private List<DevelopmentPlanWeekDto> weeks = new ArrayList<>();
}
