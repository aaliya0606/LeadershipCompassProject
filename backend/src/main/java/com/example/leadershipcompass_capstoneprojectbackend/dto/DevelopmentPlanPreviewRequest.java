package com.example.leadershipcompass_capstoneprojectbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Mock survey scores used by the AI plan POC preview endpoint.
 * <p>
 * This DTO belongs to the development-plan POC flow and is not part of the
 * survey submission API.
 */
@Data
public class DevelopmentPlanPreviewRequest {
    private String fullName;

    @NotNull
    @Min(0)
    @Max(50)
    private Integer caringTimeScore;

    @NotNull
    @Min(0)
    @Max(50)
    private Integer receivingValueScore;

    @NotNull
    @Min(0)
    @Max(50)
    private Integer actsOfSupportScore;

    @NotNull
    @Min(0)
    @Max(50)
    private Integer wordsOfRecognitionScore;

    @NotNull
    @Min(0)
    @Max(50)
    private Integer psychologicalTouchScore;
}
