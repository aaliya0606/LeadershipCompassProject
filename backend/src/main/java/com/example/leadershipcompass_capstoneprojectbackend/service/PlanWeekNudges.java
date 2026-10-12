package com.example.leadershipcompass_capstoneprojectbackend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Which week-start reminders are due for a plan. Week 1 is the welcome email.
 */
public final class PlanWeekNudges {

    private PlanWeekNudges() {
    }

    /**
     * Weeks 2–5 whose start instant is at or before {@code now}.
     * Week {@code n} starts {@code n - 1} weeks after the plan was generated.
     *
     * @param generatedAt when the plan was created
     * @param now         current instant
     * @return due week numbers, in order
     */
    public static List<Integer> dueWeeks(Instant generatedAt, Instant now) {
        List<Integer> weeks = new ArrayList<>();
        if (generatedAt == null || now == null) {
            return weeks;
        }
        for (int week = 2; week <= 5; week++) {
            Instant due = generatedAt.plus((week - 1L) * 7, ChronoUnit.DAYS);
            if (!now.isBefore(due)) {
                weeks.add(week);
            }
        }
        return weeks;
    }
}
