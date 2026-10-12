package com.example.leadershipcompass_capstoneprojectbackend.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlan;
import com.example.leadershipcompass_capstoneprojectbackend.repository.DevelopmentPlanRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanNudgeSchedulerTest {

    @Mock
    private DevelopmentPlanRepository developmentPlanRepository;

    @Mock
    private PlanEmailService planEmailService;

    @Test
    void sendsOnlyWeeksThatHaveStartedForTheCurrentPlan() {
        Instant now = Instant.parse("2026-02-01T00:00:00Z");
        DevelopmentPlan plan = new DevelopmentPlan();
        plan.setId(7L);
        plan.setGeneratedAt(now.minus(14, ChronoUnit.DAYS));
        when(developmentPlanRepository.findLatestPerUser()).thenReturn(List.of(plan));

        new PlanNudgeScheduler(developmentPlanRepository, planEmailService).sendDueWeekNudges(now);

        verify(planEmailService).sendWeekStart(7L, 2);
        verify(planEmailService).sendWeekStart(7L, 3);
        verify(planEmailService, never()).sendWeekStart(7L, 4);
        verify(planEmailService, never()).sendWeekStart(7L, 1);
    }
}
