package com.example.leadershipcompass_capstoneprojectbackend.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Sends email through SMTP (Gmail by default, or another host such as Microsoft 365).
 * The mailbox username and app password come from the environment.
 */
public class SmtpPlanMailTransport implements PlanMailTransport {

    private final JavaMailSender mailSender;

    public SmtpPlanMailTransport(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String send(PlanMailMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(new InternetAddress(message.fromAddress(), message.fromName(), "UTF-8"));
            helper.setReplyTo(message.replyTo());
            helper.setTo(message.toAddress());
            helper.setSubject(message.subject());
            helper.setText(message.textBody(), message.htmlBody());
            if (message.unsubscribeUrl() != null && !message.unsubscribeUrl().isBlank()) {
                mimeMessage.setHeader("List-Unsubscribe", "<" + message.unsubscribeUrl() + ">");
                mimeMessage.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
            }
            mailSender.send(mimeMessage);
            return mimeMessage.getMessageID();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("SMTP send failed: " + ex.getMessage(), ex);
        }
    }
}
