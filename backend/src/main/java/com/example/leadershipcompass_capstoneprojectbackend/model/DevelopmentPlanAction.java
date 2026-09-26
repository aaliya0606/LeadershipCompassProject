package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * One practical action item inside a {@link DevelopmentPlanWeek}.
 * <p>
 * Stored as an embeddable collection so existing action text in
 * {@code development_plan_week_actions} is retained, with completion state
 * added beside it.
 */
@Embeddable
@Getter
@Setter
public class DevelopmentPlanAction {

    /** Action wording shown to the learner. */
    @Column(name = "action_item", length = 1000)
    private String text;

    /** Whether the learner has checked this action off. */
    @Column(name = "completed", nullable = false, columnDefinition = "boolean default false not null")
    private boolean completed;

    /** When the action was last marked complete; cleared when unchecked. */
    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * Creates an unchecked action with the given wording.
     *
     * @param text action wording
     * @return new unchecked action
     */
    public static DevelopmentPlanAction pending(String text) {
        DevelopmentPlanAction action = new DevelopmentPlanAction();
        action.setText(text);
        action.setCompleted(false);
        action.setCompletedAt(null);
        return action;
    }
}
