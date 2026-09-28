package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.time.Instant;
import lombok.Data;

/**
 * API representation of one checkable action inside a development-plan week.
 */
@Data
public class DevelopmentPlanActionDto {

    /** Zero-based index of this action within its week. Used by the PATCH endpoint. */
    private Integer index;

    /** Action wording shown to the learner. */
    private String text;

    /** Whether the learner has checked this action off. */
    private boolean completed;

    /** When the action was last marked complete; {@code null} when unchecked. */
    private Instant completedAt;
}
