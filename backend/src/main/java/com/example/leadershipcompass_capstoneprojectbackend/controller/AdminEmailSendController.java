package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.EmailSendDto;
import com.example.leadershipcompass_capstoneprojectbackend.service.EmailSendService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST API for the outbound email send ledger.
 */
@RestController
@RequestMapping("/api/admin/email-sends")
@RequiredArgsConstructor
public class AdminEmailSendController {

    private final EmailSendService emailSendService;

    /**
     * Lists outbound email metadata. Message bodies are not stored.
     *
     * @param userId optional user filter
     * @param planId optional plan filter
     * @return newest-first ledger rows
     */
    @GetMapping
    public List<EmailSendDto> listEmailSends(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long planId) {
        return emailSendService.list(userId, planId);
    }
}
