package com.example.leadershipcompass_capstoneprojectbackend.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * API representation of a single week inside a development plan.
 */
@Data
public class DevelopmentPlanWeekDto {
    private Integer weekNumber;
    private Long moduleId;
    private String category;
    private String moduleTitle;
    private String focus;
    private String rationale;
    private List<String> actions = new ArrayList<>();
}
