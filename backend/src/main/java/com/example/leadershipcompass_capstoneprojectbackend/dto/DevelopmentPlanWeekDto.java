package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * API representation of a single week inside a development plan.
 */
@Data
public class DevelopmentPlanWeekDto {

    /** Week number within the plan (1–5). */
    private Integer weekNumber;

    /** Module library id selected for this week. */
    private Long moduleId;

    /** Leadership language category for the selected module. */
    private String category;

    /** Module title at generation time. */
    private String moduleTitle;

    /** Short focus statement for the week. */
    private String focus;

    /** Explanation of why this module was chosen for the user. */
    private String rationale;

    /** Practical action items for the learner to complete this week. */
    private List<String> actions = new ArrayList<>();
}
