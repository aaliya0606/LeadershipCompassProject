package com.example.leadershipcompass_capstoneprojectbackend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AiBrainChatRequestDto;
import com.example.leadershipcompass_capstoneprojectbackend.dto.AiBrainChatResponseDto;
import com.example.leadershipcompass_capstoneprojectbackend.service.AiBrainService;
import com.example.leadershipcompass_capstoneprojectbackend.service.AiBrainUsageLimiter;
import java.security.Principal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for the chatbot facade over {@link AiBrainService}.
 */
@ExtendWith(MockitoExtension.class)
class AiBrainControllerTest {

    @Mock
    private AiBrainService aiBrainService;

    @Mock
    private AiBrainUsageLimiter usageLimiter;

    @Mock
    private AiBrainUsageLimiter.Permit permit;

    private AiBrainController controller;

    @BeforeEach
    void setUp() {
        controller = new AiBrainController(aiBrainService, usageLimiter);
        lenient().when(usageLimiter.acquire(any(), anyString())).thenReturn(permit);
    }

    @Test
    void scopesConversationIdToAuthenticatedUser() {
        when(aiBrainService.isEnabled()).thenReturn(true);
        when(aiBrainService.chat(eq("What is Caring Time?"), eq("chat-widget-sam-test.com-abc-123"), isNull()))
                .thenReturn(Optional.of("Caring Time is one of the five leadership languages."));

        AiBrainChatRequestDto request = new AiBrainChatRequestDto();
        request.setQuery("What is Caring Time?");
        request.setConversationId("abc-123");

        AiBrainChatResponseDto response = controller.chat(request, principal("sam@test.com"));

        assertEquals("Caring Time is one of the five leadership languages.", response.getAnswer());
        verify(aiBrainService).chat("What is Caring Time?", "chat-widget-sam-test.com-abc-123", null);
    }

    @Test
    void ignoresUnsafeClientConversationIds() {
        assertEquals(
                "chat-widget-sam-test.com-default",
                AiBrainController.widgetConversationId("sam@test.com", "../other-user"));
        assertEquals(
                "chat-widget-sam-test.com-session-1",
                AiBrainController.widgetConversationId("sam@test.com", "session-1"));
    }

    @Test
    void rejectsUnauthenticatedCalls() {
        AiBrainChatRequestDto request = new AiBrainChatRequestDto();
        request.setQuery("hello");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.chat(request, null));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void returnsServiceUnavailableWhenDisabled() {
        when(aiBrainService.isEnabled()).thenReturn(false);
        AiBrainChatRequestDto request = new AiBrainChatRequestDto();
        request.setQuery("hello");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.chat(request, principal("sam@test.com")));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }

    private Principal principal(String email) {
        return () -> email;
    }
}
