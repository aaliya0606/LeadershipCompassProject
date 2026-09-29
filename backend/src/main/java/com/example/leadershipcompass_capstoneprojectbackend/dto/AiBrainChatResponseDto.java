package com.example.leadershipcompass_capstoneprojectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned by Leadership Compass after an AI-Brain chat call.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiBrainChatResponseDto {

    /** Raw answer text from the AI-Brain. */
    private String answer;
}
