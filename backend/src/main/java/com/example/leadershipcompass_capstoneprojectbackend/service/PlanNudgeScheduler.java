package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlan;
import com.example.leadershipcompass_capstoneprojectbackend.repository.DevelopmentPlanRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sends week 2–5 plan reminders once each, for each user's newest plan.
 */
@Component
public class PlanNudgeScheduler {

    private static final Logger log = LoggerFactory.getLogger(PlanNudgeScheduler.class);

    private final DevelopmentPlanRepository developmentPlanRepository;
    private final PlanEmailService planEmailService;

    public PlanNudgeScheduler(
            DevelopmentPlanRepository developmentPlanRepository,
            PlanEmailService planEmailService) {
        this.developmentPlanRepository = developmentPlanRepository;
        this.planEmailService = planEmailService;
    }

    /**
     * Runs at 8:00 Australia/Sydney. Already-sent weeks are skipped by the mailer.
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Australia/Sydney")
    public void sendDueWeekNudges() {
        sendDueWeekNudges(Instant.now());
    }

    /**
     * Sends every week-start reminder that is due at {@code now}.
     *
     * @param now instant treated as the current time
     */
    public void sendDueWeekNudges(Instant now) {
        List<DevelopmentPlan> plans = developmentPlanRepository.findLatestPerUser();
        for (DevelopmentPlan plan : plans) {
            for (int week : PlanWeekNudges.dueWeeks(plan.getGeneratedAt(), now)) {
                planEmailService.sendWeekStart(plan.getId(), week);
            }
        }
        log.info("Checked week-start nudges for {} current plans.", plans.size());
    }
}
