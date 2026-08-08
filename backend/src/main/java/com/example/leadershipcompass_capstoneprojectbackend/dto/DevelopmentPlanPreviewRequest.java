package com.example.leadershipcompass_capstoneprojectbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Mock survey scores used by the AI plan POC preview endpoint.
 * <p>
 * This DTO belongs to the development-plan POC flow and is not part of the
 * survey submission API. Scores are validated to the same 0–50 range as
 * persisted survey results.
 */
@Data
public class DevelopmentPlanPreviewRequest {

    /** Optional display name included in the AI prompt. */
    private String fullName;

    /** Mock Caring Time score (0–50). */
    @NotNull
    @Min(0)
    @Max(50)
    private Integer caringTimeScore;

    /** Mock Receiving Value score (0–50). */
    @NotNull
    @Min(0)
    @Max(50)
    private Integer receivingValueScore;

    /** Mock Acts of Support score (0–50). */
    @NotNull
    @Min(0)
    @Max(50)
    private Integer actsOfSupportScore;

    /** Mock Words of Recognition score (0–50). */
    @NotNull
    @Min(0)
    @Max(50)
    private Integer wordsOfRecognitionScore;

    /** Mock Psychological Touch score (0–50). */
    @NotNull
    @Min(0)
    @Max(50)
    private Integer psychologicalTouchScore;
}
