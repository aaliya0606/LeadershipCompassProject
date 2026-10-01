package com.example.leadershipcompass_capstoneprojectbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Opt in or out of plan emails for the signed-in user.
 */
@Data
public class UpdateEmailSubscriptionRequest {

    /** True to stop plan emails, false to resume them. */
    @NotNull
    private Boolean optedOut;
}
