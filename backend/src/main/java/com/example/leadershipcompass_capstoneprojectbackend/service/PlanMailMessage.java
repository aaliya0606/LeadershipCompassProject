package com.example.leadershipcompass_capstoneprojectbackend.service;

/**
 * One outbound plan email. Not persisted; the ledger stores metadata only.
 *
 * @param fromAddress    mailbox users see, such as no-reply@theguineagroup.com.au
 * @param fromName       display name
 * @param replyTo        address replies should reach
 * @param toAddress      recipient
 * @param subject        subject line
 * @param textBody       plain-text body, including the unsubscribe link
 * @param htmlBody       HTML body, including the unsubscribe link
 * @param unsubscribeUrl absolute URL the recipient can open to opt out
 */
public record PlanMailMessage(
        String fromAddress,
        String fromName,
        String replyTo,
        String toAddress,
        String subject,
        String textBody,
        String htmlBody,
        String unsubscribeUrl) {
}
