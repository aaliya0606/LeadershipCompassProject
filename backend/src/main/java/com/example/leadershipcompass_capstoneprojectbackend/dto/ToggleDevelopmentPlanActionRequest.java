package com.example.leadershipcompass_capstoneprojectbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for checking or unchecking one development-plan action.
 */
@Data
public class ToggleDevelopmentPlanActionRequest {

    /** {@code true} to check the action off; {@code false} to undo it. */
    @NotNull
    private Boolean completed;
}
