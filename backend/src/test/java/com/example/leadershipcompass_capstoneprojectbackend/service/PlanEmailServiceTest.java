package com.example.leadershipcompass_capstoneprojectbackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class PlanEmailServiceTest {

    @Mock
    private DevelopmentPlanRepository developmentPlanRepository;

    @Mock
    private EmailSendService emailSendService;

    @Mock
    private EmailSubscriptionService emailSubscriptionService;

    @Mock
    private ObjectProvider<PlanMailTransport> mailTransport;

    @Mock
    private PlanMailTransport transport;

    private PlanEmailService planEmailService;

    @BeforeEach
    void setUp() {
        planEmailService = new PlanEmailService(
                developmentPlanRepository,
                emailSendService,
                emailSubscriptionService,
                mailTransport,
                "no-reply@theguineagroup.com.au",
                "Leadership Compass",
                "theteam@theguineagroup.com.au");
    }

    @Test
    void skipsSendWhenMailIsNotConfigured() {
        when(emailSendService.hasSent(10L, EmailSendKind.WELCOME, 0)).thenReturn(false);
        when(mailTransport.getIfAvailable()).thenReturn(null);

        planEmailService.sendWelcome(10L);

        verify(transport, never()).send(any());
        verify(emailSendService, never()).record(
                any(), any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void skipsSendWhenUserHasUnsubscribed() {
        DevelopmentPlan plan = plan();
        when(emailSendService.hasSent(10L, EmailSendKind.WELCOME, 0)).thenReturn(false);
        when(mailTransport.getIfAvailable()).thenReturn(transport);
        when(developmentPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(emailSubscriptionService.isOptedOut(plan.getUser())).thenReturn(true);

        planEmailService.sendWelcome(10L);

        verify(transport, never()).send(any());
        verify(emailSendService, never()).record(
                any(), any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void sendsThroughConfiguredTransportAndRecordsMetadataOnly() {
        DevelopmentPlan plan = plan();
        when(emailSendService.hasSent(10L, EmailSendKind.WELCOME, 0)).thenReturn(false);
        when(mailTransport.getIfAvailable()).thenReturn(transport);
        when(developmentPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(emailSubscriptionService.isOptedOut(plan.getUser())).thenReturn(false);
        when(emailSubscriptionService.unsubscribeUrl(plan.getUser()))
                .thenReturn("http://localhost:8080/api/email/unsubscribe?token=abc");
        when(transport.send(any())).thenReturn("provider-1");

        planEmailService.sendWelcome(10L);

        ArgumentCaptor<PlanMailMessage> message = ArgumentCaptor.forClass(PlanMailMessage.class);
        verify(transport).send(message.capture());
        assertEquals("no-reply@theguineagroup.com.au", message.getValue().fromAddress());
        assertEquals("theteam@theguineagroup.com.au", message.getValue().replyTo());
        assertEquals("leader@example.com", message.getValue().toAddress());
        assertTrue(message.getValue().textBody().contains("/api/email/unsubscribe?token=abc"));
        assertTrue(message.getValue().htmlBody().contains("/api/email/unsubscribe?token=abc"));
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
