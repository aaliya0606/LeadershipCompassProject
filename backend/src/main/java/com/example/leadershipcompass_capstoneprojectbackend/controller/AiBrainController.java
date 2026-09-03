package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AiBrainChatRequestDto;
import com.example.leadershipcompass_capstoneprojectbackend.dto.AiBrainChatResponseDto;
import com.example.leadershipcompass_capstoneprojectbackend.service.AiBrainService;
import jakarta.validation.Valid;
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
 * This controller exists so future features can reach the AI-Brain without
 * going through the development-plan API.
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

    /**
     * Forwards a chat prompt to the configured AI-Brain service.
     *
     * @param request chat payload
     * @return AI-Brain answer text
     */
    @PostMapping("/chat")
    public AiBrainChatResponseDto chat(@Valid @RequestBody AiBrainChatRequestDto request) {
        if (!aiBrainService.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI-Brain integration is disabled.");
        }

        return aiBrainService.chat(request.getQuery(), request.getConversationId(), request.getK())
                .map(AiBrainChatResponseDto::new)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "AI-Brain did not return a usable answer."));
    }
}
