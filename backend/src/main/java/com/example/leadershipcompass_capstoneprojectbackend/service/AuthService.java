package com.example.leadershipcompass_capstoneprojectbackend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AuthResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.LoginRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.RegisterRequest;
import com.example.leadershipcompass_capstoneprojectbackend.model.Role;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import com.example.leadershipcompass_capstoneprojectbackend.security.JwtUtil;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SIGNUP_CODE_MINUTES = 10;
    private static final int RESET_TOKEN_MINUTES = 30;
    private static final int MAX_SIGNUP_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OutboundMailService outboundMailService;
    private final String publicSiteUrl;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            OutboundMailService outboundMailService,
            @Value("${app.mail.public-site-url:https://delightful-forest-04c15c700.5.azurestaticapps.net}")
            String publicSiteUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.outboundMailService = outboundMailService;
        this.publicSiteUrl = stripTrailingSlash(publicSiteUrl);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        User existing = email.isBlank() ? null : userRepository.findByEmail(email).orElse(null);
        if (existing != null && !Boolean.FALSE.equals(existing.getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = existing == null ? new User() : existing;
        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Public registration must never grant administrator privileges.
        user.setRole(Role.USER);
        user.setDepartment(request.getDepartment());
        user.setOrganisation(request.getOrganisation() == null ? null : request.getOrganisation().trim());

        if (!outboundMailService.isConfigured()) {
            log.warn("Mail is not configured, so sign-up for {} does not require an email code.", email);
            user.setEmailVerified(true);
            clearSignupCode(user);
            userRepository.save(user);
            return new AuthResponse(
                    null,
                    user.getRole().name(),
                    "Successfully created a new user with role" + user.getRole(),
                    null,
                    false);
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        user.setEmailVerified(false);
        user.setSignupCodeHash(sha256(code));
        user.setSignupCodeExpiresAt(Instant.now().plus(SIGNUP_CODE_MINUTES, ChronoUnit.MINUTES));
        user.setSignupCodeAttempts(0);
        userRepository.save(user);

        String text = "Your Leadership Compass confirmation code is " + code
                + ".\n\nIt expires in 10 minutes."
                + "\n\nIf you did not create an account, you can ignore this email.";
        String html = "<p>Your Leadership Compass confirmation code is <strong>" + code + "</strong>.</p>"
                + "<p>It expires in 10 minutes.</p>"
                + "<p>If you did not create an account, you can ignore this email.</p>";
        try {
            outboundMailService.send(user.getEmail(), "Confirm your Leadership Compass account", text, html);
        } catch (RuntimeException ex) {
            log.warn("Sign-up code for {} could not be sent.", user.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not send the confirmation code. Try again.");
        }

        return new AuthResponse(
                null,
                null,
                "Enter the code sent to your email.",
                null,
                true);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Confirm your email before signing in.");
        }

        return issuedToken(user, "Successfully logged in");
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthResponse verifySignupCode(String email, String code) {
        if (email == null || email.isBlank() || code == null || !code.trim().matches("\\d{6}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "That code is invalid or has expired.");
        }
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "That code is invalid or has expired."));
        int attempts = user.getSignupCodeAttempts() == null ? 0 : user.getSignupCodeAttempts();
        boolean expired = user.getSignupCodeHash() == null
                || user.getSignupCodeExpiresAt() == null
                || Instant.now().isAfter(user.getSignupCodeExpiresAt())
                || attempts >= MAX_SIGNUP_ATTEMPTS;
        if (expired || !hashesMatch(user.getSignupCodeHash(), sha256(code.trim()))) {
            if (!expired) {
                int nextAttempts = attempts + 1;
                if (nextAttempts >= MAX_SIGNUP_ATTEMPTS) {
                    clearSignupCode(user);
                } else {
                    user.setSignupCodeAttempts(nextAttempts);
                }
                userRepository.save(user);
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "That code is invalid or has expired.");
        }
        user.setEmailVerified(true);
        clearSignupCode(user);
        userRepository.save(user);
        return new AuthResponse(
                null,
                user.getRole().name(),
                "Email confirmed. You can sign in.",
                null,
                false);
    }

    /**
     * Emails a reset link when the address belongs to an account and mail is configured.
     * Missing accounts and send failures are not reported to the caller.
     *
     * @param email address entered on the forgot-password form
     */
    @Transactional
    public void forgotPassword(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        User user = userRepository.findByEmail(email.trim()).orElse(null);
        if (user == null) {
            return;
        }
        if (!outboundMailService.isConfigured()) {
            log.warn("Password reset for {} was skipped because mail is not configured.", user.getEmail());
            return;
        }
        String token = newToken();
        user.setPasswordResetTokenHash(sha256(token));
        user.setPasswordResetExpiresAt(Instant.now().plus(RESET_TOKEN_MINUTES, ChronoUnit.MINUTES));
        userRepository.save(user);

        String link = publicSiteUrl + "/reset-password.html?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
        String text = "Use this link to reset your Leadership Compass password. It expires in 30 minutes.\n\n"
                + link
                + "\n\nIf you did not ask for this, you can ignore this email.";
        String html = "<p>Use this link to reset your Leadership Compass password. It expires in 30 minutes.</p>"
                + "<p><a href=\"" + escape(link) + "\">Reset password</a></p>"
                + "<p>If you did not ask for this, you can ignore this email.</p>";
        try {
            outboundMailService.send(user.getEmail(), "Reset your Leadership Compass password", text, html);
        } catch (RuntimeException ex) {
            log.warn("Password reset email for {} could not be sent.", user.getEmail());
            throw ex;
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters.");
        }
        if (token == null || token.isBlank()) {
            throw invalidReset();
        }
        User user = userRepository.findByPasswordResetTokenHash(sha256(token.trim()))
                .orElseThrow(AuthService::invalidReset);
        if (user.getPasswordResetExpiresAt() == null || Instant.now().isAfter(user.getPasswordResetExpiresAt())) {
            throw invalidReset();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(null);
        clearSignupCode(user);
        userRepository.save(user);
    }

    private AuthResponse issuedToken(User user, String message) {
        String token = jwtUtil.generateToken(user);
        String preview = token.substring(0, Math.min(30, token.length())) + "...";
        return new AuthResponse(token, user.getRole().name(), message, preview, false);
    }

    private static void clearSignupCode(User user) {
        user.setSignupCodeHash(null);
        user.setSignupCodeExpiresAt(null);
        user.setSignupCodeAttempts(0);
    }

    private static ResponseStatusException invalidReset() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "That reset link is invalid or has expired.");
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }

    private static boolean hashesMatch(String expectedHex, String actualHex) {
        if (expectedHex == null || actualHex == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expectedHex.getBytes(StandardCharsets.UTF_8),
                actualHex.getBytes(StandardCharsets.UTF_8));
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "https://delightful-forest-04c15c700.5.azurestaticapps.net";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
