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
import com.example.leadershipcompass_capstoneprojectbackend.repository.AdminLoginChallengeRepository;
import com.example.leadershipcompass_capstoneprojectbackend.model.AdminLoginChallenge;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SIGNUP_CODE_MINUTES = 10;
    private static final int RESET_TOKEN_MINUTES = 30;
    private static final int MAX_SIGNUP_ATTEMPTS = 5;

    private static final int ADMIN_CODE_MINUTES = 10;
    private static final int MAX_ADMIN_CODE_ATTEMPTS = 5;
    private static final int ADMIN_CODE_RANGE = 1_000_000;

    private final UserRepository userRepository;
    /**
     * Stores temporary administrator login challenges.
     * Used to require email verification before issuing a TGG_ADMIN JWT.
     */
    private final AdminLoginChallengeRepository adminLoginChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OutboundMailService outboundMailService;
    private final String publicSiteUrl;
    /**
     * Secret used to protect administrator email verification codes.
     */
    private final String adminTwoFactorSecret;

    public AuthService(
            UserRepository userRepository,
            AdminLoginChallengeRepository adminLoginChallengeRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            OutboundMailService outboundMailService,
            @Value("${app.mail.public-site-url:https://delightful-forest-04c15c700.5.azurestaticapps.net}")
            String publicSiteUrl,
            @Value("${app.auth.admin-2fa-secret:}")
            String adminTwoFactorSecret) {


        this.userRepository = userRepository;
        this.adminLoginChallengeRepository = adminLoginChallengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.outboundMailService = outboundMailService;
        this.publicSiteUrl = stripTrailingSlash(publicSiteUrl);
        this.adminTwoFactorSecret = adminTwoFactorSecret;

    }

    /**
     * Generates a cryptographically secure six-digit verification code.
     *
     * Leading zeros are preserved, so every code contains exactly
     * six digits (for example, 004821).
     *
     * @return randomly generated six-digit verification code
     */
    private String generateAdminVerificationCode() {
        int number = RANDOM.nextInt(ADMIN_CODE_RANGE);
        return String.format(java.util.Locale.ROOT, "%06d", number);
    }

    /**
     * Creates a keyed HMAC-SHA256 hash for an administrator verification code.
     *
     * The challenge token is included so that identical six-digit codes
     * generated for different login challenges have different hashes.
     *
     * @param challengeTokenHash hash of the random login challenge token
     * @param code six-digit verification code
     * @return hexadecimal HMAC-SHA256 hash
     */
    private String hashAdminVerificationCode(
            String challengeTokenHash,
            String code) {

        if (adminTwoFactorSecret == null
                || adminTwoFactorSecret.isBlank()
                || adminTwoFactorSecret.length() < 32) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Administrator verification is not configured.");
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec key = new SecretKeySpec(
                    adminTwoFactorSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");

            mac.init(key);

            byte[] hash = mac.doFinal(
                    (challengeTokenHash + ":" + code)
                            .getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to protect administrator verification code.",
                    exception);
        }
    }

    /**
     * Generates a cryptographically secure, unpredictable login challenge token.
     *
     * The token identifies a particular administrator login attempt after
     * the administrator has successfully entered their password.
     *
     * The raw token is returned to the browser, while only its SHA-256 hash
     * is stored in the database.
     *
     * @return a random 256-bit challenge token encoded as hexadecimal
     */
    private String generateAdminChallengeToken() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);

        return HexFormat.of().formatHex(randomBytes);
    }

    /**
     * Creates a temporary email verification challenge after a TGG
     * administrator has successfully entered their password.
     *
     * A JWT is not issued at this stage. The administrator must verify
     * the emailed code before authentication can be completed.
     *
     * @param user administrator who passed password authentication
     * @return pending authentication response containing a challenge token
     */
    private AuthResponse createAdminLoginChallenge(User user) {

        // Only TGG administrators may enter this verification flow.
        if (user.getRole() != Role.TGG_ADMIN) {
            throw new IllegalArgumentException(
                    "Administrator verification requires a TGG_ADMIN account.");
        }

        // Never bypass verification when the email service is unavailable.
        if (!outboundMailService.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Administrator email verification is unavailable.");
        }

        // Limit the number of verification emails requested per administrator.
        long recentChallenges =
                adminLoginChallengeRepository.countByUserIdAndCreatedAtAfter(
                        user.getId(),
                        Instant.now().minus(15, ChronoUnit.MINUTES));

        if (recentChallenges >= 3) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many verification requests. Please try again later.");
        }

        // Generate the emailed code and a separate browser challenge token.
        String code = generateAdminVerificationCode();
        String challengeToken = generateAdminChallengeToken();

        // Store only protected representations of both values.
        String tokenHash = sha256(challengeToken);
        String codeHash = hashAdminVerificationCode(tokenHash, code);

        Instant now = Instant.now();

        AdminLoginChallenge challenge = new AdminLoginChallenge();
        challenge.setUser(user);
        challenge.setChallengeTokenHash(tokenHash);
        challenge.setCodeHash(codeHash);
        challenge.setCreatedAt(now);
        challenge.setExpiresAt(now.plus(ADMIN_CODE_MINUTES, ChronoUnit.MINUTES));
        challenge.setAttempts(0);

        // Persist the challenge before returning it to the browser.
        adminLoginChallengeRepository.save(challenge);

        String subject = "Leadership Compass - Administrator Login Code";

        String textBody =
                "Your Leadership Compass administrator verification code is: "
                        + code
                        + "\n\nThis code expires in 10 minutes."
                        + "\nIf you did not attempt to sign in, please contact"
                        + " your administrator.";

        String htmlBody =
                "<p>Your Leadership Compass administrator verification code is:</p>"
                        + "<h2>" + code + "</h2>"
                        + "<p>This code expires in 10 minutes.</p>"
                        + "<p>If you did not attempt to sign in, please contact"
                        + " your administrator.</p>";

        try {
            // Reuse the application's existing email transport.
            outboundMailService.send(
                    user.getEmail(),
                    subject,
                    textBody,
                    htmlBody);

            } catch (Exception exception) {
                log.error(
                        "Unable to send administrator login verification email",
                        exception
                );

                // Remove the unused challenge if the email could not be sent.
                adminLoginChallengeRepository.delete(challenge);

                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Unable to send administrator verification code."
                );
            }

        // No JWT is issued until the verification endpoint succeeds.
        return new AuthResponse(
                null,
                user.getRole().name(),
                "Enter the six-digit code sent to your email.",
                null,
                true,
                challengeToken);
    }

    /**
     * Completes TGG administrator login after successful email verification.
     *
     * The challenge must be unexpired, unused, and within the allowed
     * number of verification attempts.
     *
     * Incorrect attempts are persisted because this transaction does not
     * roll back for ResponseStatusException.
     *
     * @param challengeToken temporary token from the initial login response
     * @param code six-digit code received by email
     * @return authentication response containing the administrator JWT
     */
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthResponse verifyAdminLoginCode(
            String challengeToken,
            String code) {

        // Reject missing or incorrectly formatted input.
        if (challengeToken == null
                || !challengeToken.matches("[0-9a-fA-F]{64}")
                || code == null
                || !code.matches("\\d{6}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired administrator verification code.");
        }

        // Find the challenge using the hash of the browser token.
        // Lock the database row to prevent concurrent verification.
        String tokenHash = sha256(challengeToken);

        AdminLoginChallenge challenge = adminLoginChallengeRepository
                .findWithLockByChallengeTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or expired administrator verification code."));

        Instant now = Instant.now();

        // A challenge cannot be reused, even if the code is correct.
        if (challenge.getUsedAt() != null
                || !now.isBefore(challenge.getExpiresAt())
                || challenge.getAttempts() >= MAX_ADMIN_CODE_ATTEMPTS
                || challenge.getUser().getRole() != Role.TGG_ADMIN) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired administrator verification code.");
        }

        // Compare the submitted code with its protected HMAC hash.
        String submittedHash = hashAdminVerificationCode(
                tokenHash,
                code);

        if (!hashesMatch(challenge.getCodeHash(), submittedHash)) {

            // Record the failed attempt before rejecting authentication.
            challenge.setAttempts(challenge.getAttempts() + 1);

            // Permanently invalidate the challenge after five failures.
            if (challenge.getAttempts() >= MAX_ADMIN_CODE_ATTEMPTS) {
                challenge.setUsedAt(now);
            }

            adminLoginChallengeRepository.save(challenge);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired administrator verification code.");
        }

        // Mark the challenge as consumed before issuing the JWT.
        challenge.setUsedAt(now);
        adminLoginChallengeRepository.save(challenge);

        // The administrator has now completed both authentication steps.
        return issuedToken(
                challenge.getUser(),
                "Administrator verification successful.");
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

    /**
     * Authenticates a user using their email address and password.
     *
     * Regular users receive a JWT immediately after successful authentication.
     *
     * TGG administrators must complete an additional email verification
     * challenge before receiving a JWT.
     *
     * @param request submitted login credentials
     * @return authentication result
     */
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Confirm your email before signing in.");
        }

        // TGG administrators must verify a fresh emailed code
        // before receiving their authentication JWT.
        if (user.getRole() == Role.TGG_ADMIN) {
            return createAdminLoginChallenge(user);
        }

        // Regular users continue using the existing login flow.
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
