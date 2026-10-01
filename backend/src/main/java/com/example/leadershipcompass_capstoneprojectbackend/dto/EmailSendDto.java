package com.example.leadershipcompass_capstoneprojectbackend.dto;

import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSendKind;
import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSendStatus;
import java.time.Instant;
import lombok.Data;

/**
 * Admin view of one outbound email ledger row.
 */
@Data
public class EmailSendDto {

    /** Ledger row id. */
    private Long id;

    /** User the email was sent for. */
    private Long userId;

    /** Plan this email relates to. */
    private Long planId;

    /** Welcome versus weekly reminder. */
    private EmailSendKind kind;

    /** {@code 0} for welcome, {@code 2}–{@code 5} for weekly reminders. */
    private Integer weekNumber;

    /** Recipient address captured at send time. */
    private String toAddress;

    /** From address captured at send time. */
    private String fromAddress;

    /** Email subject line. */
    private String subject;

    /** Whether the provider accepted the message. */
    private EmailSendStatus status;

    /** Provider message id when available. */
    private String providerMessageId;

    /** Short failure reason when status is FAILED. */
    private String errorSummary;

    /** When this attempt was recorded. */
    private Instant sentAt;
}
