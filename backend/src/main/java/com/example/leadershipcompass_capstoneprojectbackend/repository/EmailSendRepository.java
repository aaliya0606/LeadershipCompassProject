package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSend;
import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSendKind;
import com.example.leadershipcompass_capstoneprojectbackend.model.EmailSendStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for the outbound email send ledger.
 */
public interface EmailSendRepository extends JpaRepository<EmailSend, Long> {

    /**
     * Finds the ledger row for one plan reminder slot.
     *
     * @param planId     development plan id
     * @param kind       welcome or week-start
     * @param weekNumber {@code 0} for welcome, otherwise the week number
     * @return existing row when present
     */
    Optional<EmailSend> findByPlanIdAndKindAndWeekNumber(Long planId, EmailSendKind kind, Integer weekNumber);

    /**
     * Whether a successful send already exists for this plan reminder slot.
     *
     * @param planId     development plan id
     * @param kind       welcome or week-start
     * @param weekNumber {@code 0} for welcome, otherwise the week number
     * @param status     expected status
     * @return {@code true} when a matching row exists
     */
    boolean existsByPlanIdAndKindAndWeekNumberAndStatus(
            Long planId, EmailSendKind kind, Integer weekNumber, EmailSendStatus status);

    /**
     * Lists sends for a user, newest first.
     *
     * @param userId user id
     * @return ledger rows
     */
    List<EmailSend> findByUserIdOrderBySentAtDesc(Long userId);

    /**
     * Lists sends for a plan, newest first.
     *
     * @param planId development plan id
     * @return ledger rows
     */
    List<EmailSend> findByPlanIdOrderBySentAtDesc(Long planId);

    /**
     * Lists sends for a user and plan, newest first.
     *
     * @param userId user id
     * @param planId development plan id
     * @return ledger rows
     */
    List<EmailSend> findByUserIdAndPlanIdOrderBySentAtDesc(Long userId, Long planId);

    /**
     * Lists all sends, newest first.
     *
     * @return ledger rows
     */
    List<EmailSend> findAllByOrderBySentAtDesc();
}
