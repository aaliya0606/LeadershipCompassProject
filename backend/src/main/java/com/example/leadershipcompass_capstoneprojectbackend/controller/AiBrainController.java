package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AiBrainChatRequestDto;
import com.example.leadershipcompass_capstoneprojectbackend.dto.AiBrainChatResponseDto;
import com.example.leadershipcompass_capstoneprojectbackend.service.AiBrainService;
import com.example.leadershipcompass_capstoneprojectbackend.service.AiBrainUsageLimiter;
import com.example.leadershipcompass_capstoneprojectbackend.service.AiBrainUsageLimiter.Action;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thin HTTP facade over {@link AiBrainService}.
 * <p>
 * The 5-week development plan flow calls {@link AiBrainService} directly.
 * This controller is the authenticated entry point for the in-app chatbot.
 */
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
@RestController
@RequestMapping("/api/ai-brain")
@RequiredArgsConstructor
public class AiBrainController {

    private final AiBrainService aiBrainService;
    private final AiBrainUsageLimiter usageLimiter;

    /**
     * Forwards a chat prompt to the configured AI-Brain service.
     * Conversation ids are scoped to the authenticated user so one person
     * cannot attach to another user's AI-Brain history.
     *
     * @param request   chat payload
     * @param principal current authenticated user
     * @return AI-Brain answer text
     */
    @PostMapping("/chat")
    public AiBrainChatResponseDto chat(
            @Valid @RequestBody AiBrainChatRequestDto request,
            Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        if (!aiBrainService.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI-Brain integration is disabled.");
        }

        try (AiBrainUsageLimiter.Permit ignored = usageLimiter.acquire(Action.CHAT, principal.getName())) {
            String conversationId = widgetConversationId(principal.getName(), request.getConversationId());
            return aiBrainService.chat(request.getQuery(), conversationId, request.getK())
                    .map(AiBrainChatResponseDto::new)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_GATEWAY,
                            "AI-Brain did not return a usable answer."));
        }
    }

    /**
     * Builds a user-scoped conversation key. Optional client session keys are
     * kept only when they are short and alphanumeric, then prefixed with the
     * caller's identity.
     *
     * @param email      authenticated user email
     * @param sessionKey optional client session token
     * @return conversation id sent to the AI-Brain
     */
    static String widgetConversationId(String email, String sessionKey) {
        String userPart = email == null
                ? "anonymous"
                : email.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "-");
        String session = "default";
        if (sessionKey != null && sessionKey.matches("[A-Za-z0-9_-]{1,64}")) {
            session = sessionKey;
        }
        return "chat-widget-" + userPart + "-" + session;
    }
}
