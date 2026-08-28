package com.example.leadershipcompass_capstoneprojectbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for calling the AI-Brain chat endpoint through Leadership Compass.
 */
@Data
public class AiBrainChatRequestDto {

    /** Prompt text sent to the AI-Brain. */
    @NotBlank
    private String query;

    /** Optional conversation key used by the AI-Brain for multi-turn context. */
    private String conversationId;

    /** Optional retrieval depth passed through to the AI-Brain. */
    private Integer k;
}
