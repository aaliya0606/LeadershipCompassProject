package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlan;
import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSendKind;
import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSendStatus;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.DevelopmentPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Sends plan emails through the shared outbound mailer when it is configured.
 * Failures and a missing transport are recorded or skipped without failing plan generation.
 */
@Service
public class PlanEmailService {

    private static final Logger log = LoggerFactory.getLogger(PlanEmailService.class);

    private final DevelopmentPlanRepository developmentPlanRepository;
    private final EmailSendService emailSendService;
    private final EmailSubscriptionService emailSubscriptionService;
    private final OutboundMailService outboundMailService;

    public PlanEmailService(
            DevelopmentPlanRepository developmentPlanRepository,
            EmailSendService emailSendService,
            EmailSubscriptionService emailSubscriptionService,
            OutboundMailService outboundMailService) {
        this.developmentPlanRepository = developmentPlanRepository;
        this.emailSendService = emailSendService;
        this.emailSubscriptionService = emailSubscriptionService;
        this.outboundMailService = outboundMailService;
    }

    /**
     * Sends the welcome email for a newly generated plan when mail is configured,
     * the user is still subscribed, and a SENT row does not already exist.
     *
     * @param planId newly saved plan id
     */
    @Transactional
    public void sendWelcome(Long planId) {
        try {
            send(
                    planId,
                    EmailSendKind.WELCOME,
                    0,
                    "Your 5-week Leadership Compass plan is ready",
                    "Your 5-week Leadership Compass plan is ready.",
                    "Open the 5 Week Plan page to see this week's actions.");
        } catch (RuntimeException ex) {
            log.warn("Welcome email for plan {} was not sent.", planId, ex);
        }
    }

    /**
     * Sends a week-start reminder for weeks 2–5 when mail is configured.
     *
     * @param planId     development plan id
     * @param weekNumber week number (2–5)
     */
    @Transactional
    public void sendWeekStart(Long planId, int weekNumber) {
        if (weekNumber < 2 || weekNumber > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Week-start emails are only sent for weeks 2–5.");
        }
        try {
            send(
                    planId,
                    EmailSendKind.WEEK_START,
                    weekNumber,
                    "Week " + weekNumber + " of your Leadership Compass plan",
                    "Week " + weekNumber + " of your Leadership Compass plan is starting.",
                    "Open the 5 Week Plan page for this week's actions.");
        } catch (RuntimeException ex) {
            log.warn("Week {} email for plan {} was not sent.", weekNumber, planId, ex);
        }
    }

    private void send(
            Long planId,
            EmailSendKind kind,
            int weekNumber,
            String subject,
            String intro,
            String detail) {
        if (emailSendService.hasSent(planId, kind, weekNumber)) {
            return;
        }
        if (!outboundMailService.isConfigured()) {
            log.info("Skipping {} email for plan {} because mail is not configured.", kind, planId);
            return;
        }
        DevelopmentPlan plan = developmentPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Development plan not found."));
        User user = plan.getUser();
        if (emailSubscriptionService.isOptedOut(user)) {
            log.info("Skipping {} email for plan {} because {} has unsubscribed.", kind, planId, user.getEmail());
            return;
        }
        String unsubscribeUrl = emailSubscriptionService.unsubscribeUrl(user);
        String text = intro + "\n\n" + detail
                + "\n\nYou are receiving this because you have a Leadership Compass account."
                + "\nUnsubscribe: " + unsubscribeUrl;
        String html = "<p>" + escape(intro) + "</p><p>" + escape(detail) + "</p>"
                + "<p>You are receiving this because you have a Leadership Compass account. "
                + "<a href=\"" + escape(unsubscribeUrl) + "\">Unsubscribe</a></p>";
        try {
            String providerMessageId = outboundMailService.send(
                    user.getEmail(),
                    subject,
                    text,
                    html,
                    unsubscribeUrl);
            emailSendService.record(
                    plan,
                    kind,
                    weekNumber,
                    user.getEmail(),
                    outboundMailService.fromAddress(),
                    subject,
                    EmailSendStatus.SENT,
                    providerMessageId,
                    null);
        } catch (RuntimeException ex) {
            log.warn("{} email for plan {} could not be sent.", kind, plan.getId(), ex);
            emailSendService.record(
                    plan,
                    kind,
                    weekNumber,
                    user.getEmail(),
                    outboundMailService.fromAddress(),
                    subject,
                    EmailSendStatus.FAILED,
                    null,
                    ex.getMessage());
        }
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
