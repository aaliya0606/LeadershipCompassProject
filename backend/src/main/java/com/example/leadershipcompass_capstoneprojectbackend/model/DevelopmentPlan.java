package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * One generated 5-week development plan snapshot for a user.
 * <p>
 * Users may have many plans over time. The newest plan is the row with the
 * latest {@link #generatedAt} for that user; older rows are retained as history.
 * Week details are stored on {@link DevelopmentPlanWeek} (module id plus
 * denormalised title/category/focus/rationale/actions) so past plans remain
 * readable even if modules later change.
 */
@Entity
@Table(
        name = "development_plans",
        indexes = {
                @Index(name = "idx_development_plans_user_generated", columnList = "user_id, generatedAt")
        })
@Getter
@Setter
public class DevelopmentPlan {

    /** Surrogate primary key for this plan snapshot. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User who owns this plan. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Snapshot of Caring Time score at generation time. */
    @Column(nullable = false)
    private Integer caringTimeScore;

    /** Snapshot of Receiving Value score at generation time. */
    @Column(nullable = false)
    private Integer receivingValueScore;

    /** Snapshot of Acts of Support score at generation time. */
    @Column(nullable = false)
    private Integer actsOfSupportScore;

    /** Snapshot of Words of Recognition score at generation time. */
    @Column(nullable = false)
    private Integer wordsOfRecognitionScore;

    /** Snapshot of Psychological Touch score at generation time. */
    @Column(nullable = false)
    private Integer psychologicalTouchScore;

    /** {@code AI_BRAIN} or {@code RULE_BASED_FALLBACK}. */
    @Column(nullable = false, length = 40)
    private String generationSource;

    /** When this plan snapshot was created. */
    @Column(nullable = false)
    private Instant generatedAt = Instant.now();

    /** Ordered week rows that make up this plan snapshot. */
    @OneToMany(mappedBy = "developmentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("weekNumber ASC")
    private List<DevelopmentPlanWeek> weeks = new ArrayList<>();

    /**
     * Replaces all weekly rows for this plan and wires the parent association.
     *
     * @param newWeeks ordered week entities
     */
    public void replaceWeeks(List<DevelopmentPlanWeek> newWeeks) {
        weeks.clear();
        weeks.addAll(newWeeks);
        for (DevelopmentPlanWeek week : weeks) {
            week.setDevelopmentPlan(this);
        }
    }
}
