package com.example.leadershipcompass_capstoneprojectbackend.service;

/**
 * Outbound mail transport. No bean is registered until M365 SMTP or Graph
 * credentials are present and {@code app.mail.enabled} is true.
 */
public interface PlanMailTransport {

    /**
     * Sends one plan email.
     *
     * @param message message to send
     * @return provider message id, or {@code null} if the provider does not return one
     */
    String send(PlanMailMessage message);
}
