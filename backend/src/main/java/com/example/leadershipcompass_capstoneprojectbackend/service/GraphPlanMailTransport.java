package com.example.leadershipcompass_capstoneprojectbackend.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Sends plan email with Microsoft Graph {@code sendMail} using application
 * credentials. The unsubscribe link is in the body. Graph only accepts
 * custom {@code x-} headers, so the RFC List-Unsubscribe header is set by
 * {@link SmtpPlanMailTransport} when SMTP is the provider.
 */
public class GraphPlanMailTransport implements PlanMailTransport {

    private final RestClient restClient;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String sender;

    private String accessToken;
    private Instant accessTokenExpiresAt = Instant.EPOCH;

    public GraphPlanMailTransport(
            RestClient restClient,
            String tenantId,
            String clientId,
            String clientSecret,
            String sender) {
        this.restClient = restClient;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.sender = sender;
    }

    @Override
    public String send(PlanMailMessage message) {
        Map<String, Object> from = Map.of(
                "emailAddress", Map.of("address", message.fromAddress(), "name", message.fromName()));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("subject", message.subject());
        body.put("body", Map.of("contentType", "HTML", "content", message.htmlBody()));
        body.put("from", from);
        body.put("toRecipients", List.of(Map.of("emailAddress", Map.of("address", message.toAddress()))));
        body.put("replyTo", List.of(Map.of("emailAddress", Map.of("address", message.replyTo()))));

        restClient.post()
                .uri("https://graph.microsoft.com/v1.0/users/{sender}/sendMail", sender)
                .header("Authorization", "Bearer " + accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", body, "saveToSentItems", true))
                .retrieve()
                .toBodilessEntity();
        return null;
    }

    private synchronized String accessToken() {
        if (accessToken != null && Instant.now().isBefore(accessTokenExpiresAt)) {
            return accessToken;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("scope", "https://graph.microsoft.com/.default");
        form.add("grant_type", "client_credentials");

        TokenResponse token = restClient.post()
                .uri("https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token", tenantId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
        if (token == null || token.access_token == null || token.access_token.isBlank()) {
            throw new IllegalStateException("Microsoft Graph did not return an access token.");
        }
        accessToken = token.access_token;
        long lifetime = token.expires_in > 120 ? token.expires_in - 60 : 60;
        accessTokenExpiresAt = Instant.now().plusSeconds(lifetime);
        return accessToken;
    }

    private static final class TokenResponse {
        public String access_token;
        public long expires_in;
    }
}
