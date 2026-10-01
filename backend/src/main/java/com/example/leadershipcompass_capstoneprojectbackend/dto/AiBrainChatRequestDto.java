package com.example.leadershipcompass_capstoneprojectbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for calling the AI-Brain chat endpoint through Leadership Compass.
 */
@Data
public class AiBrainChatRequestDto {

    /** Prompt text sent to the AI-Brain. */
    @NotBlank
    @Size(max = 2000)
    private String query;

    /** Optional conversation key used by the AI-Brain for multi-turn context. */
    @Size(max = 64)
    private String conversationId;

    /** Optional retrieval depth passed through to the AI-Brain. */
    @Min(1)
    @Max(5)
    private Integer k;
}
