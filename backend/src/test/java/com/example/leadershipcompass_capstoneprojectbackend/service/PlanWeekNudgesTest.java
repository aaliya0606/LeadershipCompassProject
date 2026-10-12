package com.example.leadershipcompass_capstoneprojectbackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlanWeekNudgesTest {

    private final Instant generatedAt = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void nothingIsDueDuringTheFirstWeek() {
        assertEquals(List.of(), PlanWeekNudges.dueWeeks(generatedAt, generatedAt.plus(6, ChronoUnit.DAYS)));
    }

    @Test
    void weekTwoIsDueOneWeekAfterGeneration() {
        assertEquals(List.of(2), PlanWeekNudges.dueWeeks(generatedAt, generatedAt.plus(7, ChronoUnit.DAYS)));
    }

    @Test
    void laterWeeksAreIncludedOnceTheirStartHasPassed() {
        assertEquals(List.of(2, 3), PlanWeekNudges.dueWeeks(generatedAt, generatedAt.plus(14, ChronoUnit.DAYS)));
        assertEquals(
                List.of(2, 3, 4, 5),
                PlanWeekNudges.dueWeeks(generatedAt, generatedAt.plus(28, ChronoUnit.DAYS)));
    }
}
