package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.EmailSendDto;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlan;
import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSend;
import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSendKind;
import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSendStatus;
import com.example.leadershipcompass_capstoneprojectbackend.repository.EmailSendRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes and reads the metadata-only outbound email ledger.
 */
@Service
public class EmailSendService {

    private static final int ERROR_SUMMARY_MAX = 500;

    private final EmailSendRepository emailSendRepository;

    public EmailSendService(EmailSendRepository emailSendRepository) {
        this.emailSendRepository = emailSendRepository;
    }

    /**
     * Whether a successful send already exists for this plan reminder slot.
     *
     * @param planId     development plan id
     * @param kind       welcome or week-start
     * @param weekNumber {@code 0} for welcome
     * @return {@code true} when a SENT row exists
     */
    @Transactional(readOnly = true)
    public boolean hasSent(Long planId, EmailSendKind kind, int weekNumber) {
        return emailSendRepository.existsByPlanIdAndKindAndWeekNumberAndStatus(
                planId, kind, weekNumber, EmailSendStatus.SENT);
    }

    /**
     * Inserts or updates the ledger row for one plan reminder slot.
     * HTML and MIME are never stored.
     *
     * @param plan              plan the email relates to
     * @param kind              welcome or week-start
     * @param weekNumber        {@code 0} for welcome
     * @param toAddress         recipient snapshot
     * @param fromAddress       sender snapshot
     * @param subject           subject line
     * @param status            SENT or FAILED
     * @param providerMessageId provider id when available
     * @param errorSummary      short failure text, truncated to 500 characters
     * @return persisted ledger row
     */
    @Transactional
    public EmailSend record(
            DevelopmentPlan plan,
            EmailSendKind kind,
            int weekNumber,
            String toAddress,
            String fromAddress,
            String subject,
            EmailSendStatus status,
            String providerMessageId,
            String errorSummary) {
        EmailSend send = emailSendRepository
                .findByPlanIdAndKindAndWeekNumber(plan.getId(), kind, weekNumber)
                .orElseGet(EmailSend::new);
        send.setUser(plan.getUser());
        send.setPlan(plan);
        send.setKind(kind);
        send.setWeekNumber(weekNumber);
        send.setToAddress(toAddress);
        send.setFromAddress(fromAddress);
        send.setSubject(subject);
        send.setStatus(status);
        send.setProviderMessageId(providerMessageId);
        send.setErrorSummary(truncate(errorSummary));
        send.setSentAt(Instant.now());
        return emailSendRepository.save(send);
    }

    /**
     * Lists ledger rows for TGG, optionally filtered by user and/or plan.
     *
     * @param userId user id, or {@code null} for any user
     * @param planId plan id, or {@code null} for any plan
     * @return newest-first ledger DTOs (metadata only)
     */
    @Transactional(readOnly = true)
    public List<EmailSendDto> list(Long userId, Long planId) {
        List<EmailSend> rows;
        if (userId != null && planId != null) {
            rows = emailSendRepository.findByUserIdAndPlanIdOrderBySentAtDesc(userId, planId);
        } else if (userId != null) {
            rows = emailSendRepository.findByUserIdOrderBySentAtDesc(userId);
        } else if (planId != null) {
            rows = emailSendRepository.findByPlanIdOrderBySentAtDesc(planId);
        } else {
            rows = emailSendRepository.findAllByOrderBySentAtDesc();
        }
        return rows.stream().map(this::toDto).toList();
    }

    private EmailSendDto toDto(EmailSend send) {
        EmailSendDto dto = new EmailSendDto();
        dto.setId(send.getId());
        dto.setUserId(send.getUser().getId());
        dto.setPlanId(send.getPlan().getId());
        dto.setKind(send.getKind());
        dto.setWeekNumber(send.getWeekNumber());
        dto.setToAddress(send.getToAddress());
        dto.setFromAddress(send.getFromAddress());
        dto.setSubject(send.getSubject());
        dto.setStatus(send.getStatus());
        dto.setProviderMessageId(send.getProviderMessageId());
        dto.setErrorSummary(send.getErrorSummary());
        dto.setSentAt(send.getSentAt());
        return dto;
    }

    private static String truncate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= ERROR_SUMMARY_MAX) {
            return trimmed;
        }
        return trimmed.substring(0, ERROR_SUMMARY_MAX);
    }
}
