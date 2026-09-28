package com.example.leadershipcompass_capstoneprojectbackend.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class SmtpPlanMailTransportTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void setsReplyToAndUnsubscribeHeaders() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        SmtpPlanMailTransport transport = new SmtpPlanMailTransport(mailSender);

        transport.send(new PlanMailMessage(
                "no-reply@theguineagroup.com.au",
                "Leadership Compass",
                "theteam@theguineagroup.com.au",
                "leader@example.com",
                "Your plan is ready",
                "Unsubscribe: http://localhost:8080/api/email/unsubscribe?token=abc",
                "<p><a href=\"http://localhost:8080/api/email/unsubscribe?token=abc\">Unsubscribe</a></p>",
                "http://localhost:8080/api/email/unsubscribe?token=abc"));

        verify(mailSender).send(mimeMessage);
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        mimeMessage.writeTo(raw);
        String message = raw.toString(StandardCharsets.UTF_8);
        assertTrue(message.contains("no-reply@theguineagroup.com.au"));
        assertTrue(message.contains("theteam@theguineagroup.com.au"));
        assertTrue(message.contains("List-Unsubscribe"));
        assertTrue(message.contains("List-Unsubscribe=One-Click"));
        assertTrue(message.contains("unsubscribe?token=abc"));
    }
}
