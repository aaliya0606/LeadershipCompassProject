package com.example.leadershipcompass_capstoneprojectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Whether the signed-in user still receives plan emails.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailSubscriptionDto {

    /** True when plan emails must not be sent. */
    private boolean optedOut;
}
