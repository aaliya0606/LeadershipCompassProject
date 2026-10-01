package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Stores plan-email opt-out and builds the unsubscribe link for each user.
 */
@Service
public class EmailSubscriptionService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final String publicBaseUrl;

    public EmailSubscriptionService(
            UserRepository userRepository,
            @Value("${app.mail.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.userRepository = userRepository;
        this.publicBaseUrl = stripTrailingSlash(publicBaseUrl);
    }

    /**
     * Whether plan emails must be skipped for this user.
     *
     * @param user account that would receive the email
     * @return {@code true} when the user has opted out
     */
    public boolean isOptedOut(User user) {
        return Boolean.TRUE.equals(user.getEmailOptOut());
    }

    /**
     * Absolute unsubscribe URL for the message footer and List-Unsubscribe header.
     * Creates a token the first time it is needed.
     *
     * @param user recipient
     * @return public unsubscribe URL
     */
    @Transactional
    public String unsubscribeUrl(User user) {
        User current = userRepository.findById(user.getId()).orElse(user);
        if (current.getUnsubscribeToken() == null || current.getUnsubscribeToken().isBlank()) {
            current.setUnsubscribeToken(newToken());
            userRepository.save(current);
        }
        user.setUnsubscribeToken(current.getUnsubscribeToken());
        return publicBaseUrl + "/api/email/unsubscribe?token=" + current.getUnsubscribeToken();
    }

    /**
     * Opts the user out. Repeating the same token is a no-op success.
     *
     * @param token unsubscribe token from the email link
     */
    @Transactional
    public void unsubscribe(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsubscribe token is required.");
        }
        User user = userRepository.findByUnsubscribeToken(token.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unsubscribe link is not valid."));
        user.setEmailOptOut(true);
        userRepository.save(user);
    }

    /**
     * Sets the signed-in user's plan-email preference.
     *
     * @param email    authenticated user email
     * @param optedOut {@code true} to stop plan emails
     * @return saved preference
     */
    @Transactional
    public boolean setOptedOut(String email, boolean optedOut) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));
        user.setEmailOptOut(optedOut);
        userRepository.save(user);
        return optedOut;
    }

    /**
     * Reads the signed-in user's plan-email preference.
     *
     * @param email authenticated user email
     * @return {@code true} when plan emails are stopped
     */
    @Transactional(readOnly = true)
    public boolean isOptedOut(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));
        return isOptedOut(user);
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8080";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
