package com.example.leadershipcompass_capstoneprojectbackend.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlan;
import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSendKind;
import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSendStatus;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.DevelopmentPlanRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanEmailServiceTest {

    @Mock
    private DevelopmentPlanRepository developmentPlanRepository;

    @Mock
    private EmailSendService emailSendService;

    @Mock
    private EmailSubscriptionService emailSubscriptionService;

    @Mock
    private OutboundMailService outboundMailService;

    private PlanEmailService planEmailService;

    @BeforeEach
    void setUp() {
        planEmailService = new PlanEmailService(
                developmentPlanRepository,
                emailSendService,
                emailSubscriptionService,
                outboundMailService);
    }

    @Test
    void skipsSendWhenMailIsNotConfigured() {
        when(emailSendService.hasSent(10L, EmailSendKind.WELCOME, 0)).thenReturn(false);
        when(outboundMailService.isConfigured()).thenReturn(false);

        planEmailService.sendWelcome(10L);

        verify(outboundMailService, never()).send(any(), any(), any(), any(), any());
        verify(emailSendService, never()).record(
                any(), any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void skipsSendWhenUserHasUnsubscribed() {
        DevelopmentPlan plan = plan();
        when(emailSendService.hasSent(10L, EmailSendKind.WELCOME, 0)).thenReturn(false);
        when(outboundMailService.isConfigured()).thenReturn(true);
        when(developmentPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(emailSubscriptionService.isOptedOut(plan.getUser())).thenReturn(true);

        planEmailService.sendWelcome(10L);

        verify(outboundMailService, never()).send(any(), any(), any(), any(), any());
        verify(emailSendService, never()).record(
                any(), any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void sendsThroughConfiguredTransportAndRecordsMetadataOnly() {
        DevelopmentPlan plan = plan();
        when(emailSendService.hasSent(10L, EmailSendKind.WELCOME, 0)).thenReturn(false);
        when(outboundMailService.isConfigured()).thenReturn(true);
        when(outboundMailService.fromAddress()).thenReturn("no-reply@theguineagroup.com.au");
        when(developmentPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(emailSubscriptionService.isOptedOut(plan.getUser())).thenReturn(false);
        when(emailSubscriptionService.unsubscribeUrl(plan.getUser()))
                .thenReturn("http://localhost:8080/api/email/unsubscribe?token=abc");
        when(outboundMailService.send(any(), any(), any(), any(), any())).thenReturn("provider-1");

        planEmailService.sendWelcome(10L);

        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        verify(outboundMailService).send(
                eq("leader@example.com"),
                eq("Your 5-week Leadership Compass plan is ready"),
                text.capture(),
                html.capture(),
                eq("http://localhost:8080/api/email/unsubscribe?token=abc"));
        assertTrue(text.getValue().contains("/api/email/unsubscribe?token=abc"));
        assertTrue(html.getValue().contains("/api/email/unsubscribe?token=abc"));
        verify(emailSendService).record(
                plan,
                EmailSendKind.WELCOME,
                0,
                "leader@example.com",
                "no-reply@theguineagroup.com.au",
                "Your 5-week Leadership Compass plan is ready",
                EmailSendStatus.SENT,
                "provider-1",
                null);
    }

    private static DevelopmentPlan plan() {
        User user = new User();
        user.setId(3L);
        user.setEmail("leader@example.com");
        DevelopmentPlan plan = new DevelopmentPlan();
        plan.setId(10L);
        plan.setUser(user);
        return plan;
    }
}
