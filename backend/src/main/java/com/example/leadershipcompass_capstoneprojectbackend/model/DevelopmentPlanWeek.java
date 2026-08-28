package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * One week inside a persisted {@link DevelopmentPlan}.
 * <p>
 * {@link #moduleId} links back to the module library. Title and category are
 * also stored so historical plans stay intact if modules are later edited or
 * deactivated.
 */
@Entity
@Table(name = "development_plan_weeks")
@Getter
@Setter
public class DevelopmentPlanWeek {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Parent plan that owns this week. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "development_plan_id", nullable = false)
    private DevelopmentPlan developmentPlan;

    /** Week number within the plan (1–5). Also the recommended completion order. */
    @Column(nullable = false)
    private Integer weekNumber;

    /** Module library id at generation time (may later be inactive or renamed). */
    @Column(nullable = false)
    private Long moduleId;

    /** Leadership language category denormalised at generation time. */
    @Column(nullable = false, length = 120)
    private String category;

    /** Module title denormalised at generation time. */
    @Column(nullable = false, length = 200)
    private String moduleTitle;

    /** Short focus statement for the week. */
    @Column(length = 1000)
    private String focus;

    /** Explanation of why this module was recommended. */
    @Column(length = 2000)
    private String rationale;

    /** Ordered practical action items for the learner. */
    @ElementCollection
    @CollectionTable(name = "development_plan_week_actions", joinColumns = @JoinColumn(name = "development_plan_week_id"))
    @Column(name = "action_item", length = 1000)
    @OrderColumn(name = "action_order")
    private List<String> actions = new ArrayList<>();
}
