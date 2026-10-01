package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Metadata-only ledger of outbound plan emails.
 * <p>
 * Stores who was emailed, when, which plan/week, subject, and status.
 * HTML bodies and MIME are not persisted.
 */
@Entity
@Table(
        name = "email_sends",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_sends_plan_kind_week",
                        columnNames = {"plan_id", "kind", "week_number"})
        },
        indexes = {
                @Index(name = "idx_email_sends_user_sent_at", columnList = "user_id, sent_at")
        })
@Getter
@Setter
public class EmailSend {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User the email was sent for. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Plan this email relates to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private DevelopmentPlan plan;

    /** Welcome versus weekly reminder. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EmailSendKind kind;

    /** {@code 0} for welcome, {@code 2}–{@code 5} for weekly reminders. */
    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    /** Recipient address captured at send time. */
    @Column(name = "to_address", nullable = false, length = 255)
    private String toAddress;

    /** From address captured at send time. */
    @Column(name = "from_address", nullable = false, length = 255)
    private String fromAddress;

    /** Email subject line. */
    @Column(nullable = false, length = 255)
    private String subject;

    /** Whether the provider accepted the message. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailSendStatus status;

    /** Provider message id when the send API returns one. */
    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    /** Short failure reason; never a full stack trace. */
    @Column(name = "error_summary", length = 500)
    private String errorSummary;

    /** When this attempt was recorded. */
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
}
