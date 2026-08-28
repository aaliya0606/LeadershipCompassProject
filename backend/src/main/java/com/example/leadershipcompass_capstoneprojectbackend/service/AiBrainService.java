package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Reusable HTTP client for the TGG AI-Brain FastAPI service.
 * <p>
 * Development-plan generation and future features (for example activity critique)
 * should call this service instead of talking to the AI-Brain directly.
 */
@Service
public class AiBrainService {

    private static final Logger log = LoggerFactory.getLogger(AiBrainService.class);
    private static final int DEFAULT_K = 3;

    private final RestClient restClient;
    private final boolean enabled;

    /**
     * Creates an AI-Brain client from application configuration.
     *
     * @param baseUrl        AI-Brain base URL (for example {@code http://127.0.0.1:8000})
     * @param enabled        whether outbound AI-Brain calls are allowed
     * @param timeoutSeconds connect and read timeout in seconds
     */
    public AiBrainService(
            @Value("${app.ai-brain.base-url:http://127.0.0.1:8000}") String baseUrl,
            @Value("${app.ai-brain.enabled:true}") boolean enabled,
            @Value("${app.ai-brain.timeout-seconds:120}") long timeoutSeconds) {
        this.enabled = enabled;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(timeoutSeconds).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(timeoutSeconds).toMillis());
        this.restClient = RestClient.builder()
                .baseUrl(normalizeBaseUrl(baseUrl))
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * @return {@code true} when AI-Brain calls are enabled in configuration
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sends a chat query to the AI-Brain {@code POST /chat} endpoint.
     *
     * @param query          prompt text
     * @param conversationId conversation key for AI-Brain history (may be {@code null})
     * @return the answer text, or empty if disabled, empty, or the call failed
     */
    public Optional<String> chat(String query, String conversationId) {
        return chat(query, conversationId, DEFAULT_K);
    }

    /**
     * Sends a chat query to the AI-Brain {@code POST /chat} endpoint.
     *
     * @param query          prompt text
     * @param conversationId conversation key for AI-Brain history (may be {@code null})
     * @param k              retrieval depth passed to the AI-Brain
     * @return the answer text, or empty if disabled, empty, or the call failed
     */
    public Optional<String> chat(String query, String conversationId, Integer k) {
        if (!enabled) {
            return Optional.empty();
        }
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }

        int retrievalK = k == null || k < 1 ? DEFAULT_K : k;
        String conversation = conversationId == null || conversationId.isBlank() ? "default" : conversationId.trim();

        try {
            AiBrainChatResponse response = restClient.post()
                    .uri("/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new AiBrainChatRequest(query, retrievalK, conversation, false, ""))
                    .retrieve()
                    .body(AiBrainChatResponse.class);

            if (response == null || response.answer() == null || response.answer().isBlank()) {
                log.warn("AI-brain /chat returned an empty answer.");
                return Optional.empty();
            }
            return Optional.of(response.answer());
        } catch (RestClientResponseException ex) {
            log.warn(
                    "AI-brain /chat request failed with HTTP {}. Response: {}",
                    ex.getStatusCode().value(),
                    truncate(ex.getResponseBodyAsString(), 500));
            return Optional.empty();
        } catch (RestClientException ex) {
            log.warn("AI-brain /chat request failed before a response was received.", ex);
            return Optional.empty();
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }

    /** Wire format for the AI-Brain FastAPI {@code ChatRequest} model. */
    private record AiBrainChatRequest(
            String query,
            int k,
            @JsonProperty("conversation_id") String conversationId,
            @JsonProperty("eval_mode") boolean evalMode,
            @JsonProperty("run_id") String runId) {
    }

    /** Wire format for the AI-Brain FastAPI {@code ChatResponse} model. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AiBrainChatResponse(String answer) {
    }
}
