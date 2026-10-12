package com.example.leadershipcompass_capstoneprojectbackend.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Sends plan and account email through the configured SMTP or Graph transport.
 * From, reply-to, and the mailbox credentials are the same for every message.
 */
@Service
public class OutboundMailService {

    private final ObjectProvider<PlanMailTransport> mailTransport;
    private final String mailFrom;
    private final String mailFromName;
    private final String replyTo;

    public OutboundMailService(
            ObjectProvider<PlanMailTransport> mailTransport,
            @Value("${app.mail.from:}") String mailFrom,
            @Value("${app.mail.from-name:Leadership Compass}") String mailFromName,
            @Value("${app.mail.reply-to:theteam@theguineagroup.com.au}") String replyTo) {
        this.mailTransport = mailTransport;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
        this.replyTo = replyTo;
    }

    /**
     * Whether a transport bean is registered.
     *
     * @return {@code true} when mail is enabled and credentials are complete
     */
    public boolean isConfigured() {
        return mailTransport.getIfAvailable() != null;
    }

    /**
     * Mailbox address recipients see.
     *
     * @return configured From address
     */
    public String fromAddress() {
        return mailFrom;
    }

    /**
     * Sends an account email with no unsubscribe header.
     *
     * @param toAddress recipient
     * @param subject   subject line
     * @param textBody  plain-text body
     * @param htmlBody  HTML body
     * @return provider message id, or {@code null} when the provider does not return one
     */
    public String send(String toAddress, String subject, String textBody, String htmlBody) {
        return send(toAddress, subject, textBody, htmlBody, null);
    }

    /**
     * Sends one message. Plan emails pass an unsubscribe URL so SMTP can set the header.
     *
     * @param toAddress      recipient
     * @param subject        subject line
     * @param textBody       plain-text body
     * @param htmlBody       HTML body
     * @param unsubscribeUrl absolute unsubscribe URL, or null
     * @return provider message id, or {@code null} when the provider does not return one
     */
    public String send(
            String toAddress,
            String subject,
            String textBody,
            String htmlBody,
            String unsubscribeUrl) {
        PlanMailTransport transport = mailTransport.getIfAvailable();
        if (transport == null) {
            throw new IllegalStateException("Mail is not configured.");
        }
        return transport.send(new PlanMailMessage(
                mailFrom,
                mailFromName,
                replyTo,
                toAddress,
                subject,
                textBody,
                htmlBody,
                unsubscribeUrl));
    }
}
