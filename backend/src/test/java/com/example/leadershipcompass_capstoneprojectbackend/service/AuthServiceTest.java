package com.example.leadershipcompass_capstoneprojectbackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AuthResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.LoginRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.RegisterRequest;
import com.example.leadershipcompass_capstoneprojectbackend.model.Role;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.AdminLoginChallengeRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import com.example.leadershipcompass_capstoneprojectbackend.security.JwtUtil;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import com.example.leadershipcompass_capstoneprojectbackend.model.AdminLoginChallenge;
import static org.mockito.Mockito.doThrow;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OutboundMailService outboundMailService;

    @Mock
    private AdminLoginChallengeRepository adminLoginChallengeRepository;

    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                adminLoginChallengeRepository,
                passwordEncoder,
                jwtUtil,
                outboundMailService,
                "https://leadership.example",
                "test-only-admin-2fa-secret-at-least-32-characters");
        user = User.builder()
                .email("leader@example.com")
                .password("stored")
                .role(Role.USER)
                .fullName("Leader")
                .build();
        lenient().when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void loginReturnsTokenWithoutSendingMail() {
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "stored")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("signed-token-value");

        AuthResponse response = authService.login(loginRequest());

        assertFalse(response.isTwoFactorRequired());
        assertEquals("signed-token-value", response.getToken());
        verify(outboundMailService, never()).send(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void loginRejectsAnUnconfirmedAccount() {
        user.setEmailVerified(false);
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "stored")).thenReturn(true);

        RuntimeException error = assertThrows(RuntimeException.class, () -> authService.login(loginRequest()));

        assertEquals("Confirm your email before signing in.", error.getMessage());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void signUpWithMailSendsACodeAndDoesNotConfirmTheAccount() {
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.empty());
        when(outboundMailService.isConfigured()).thenReturn(true);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");

        AuthResponse response = authService.register(registerRequest());

        assertTrue(response.isTwoFactorRequired());
        assertNull(response.getToken());
        verify(outboundMailService).send(
                eq("leader@example.com"),
                eq("Confirm your Leadership Compass account"),
                anyString(),
                anyString());
    }

    @Test
    void correctSignUpCodeConfirmsTheAccountAndWrongCodeDoesNot() {
        User[] saved = new User[1];
        when(userRepository.save(any())).thenAnswer(invocation -> {
            saved[0] = invocation.getArgument(0);
            return saved[0];
        });
        when(userRepository.findByEmail("leader@example.com")).thenAnswer(invocation ->
                saved[0] == null ? Optional.empty() : Optional.of(saved[0]));
        when(outboundMailService.isConfigured()).thenReturn(true);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");

        authService.register(registerRequest());
        String code = sentCode();
        String wrong = "000000".equals(code) ? "111111" : "000000";

        assertThrows(ResponseStatusException.class, () -> authService.verifySignupCode("leader@example.com", wrong));
        assertEquals(1, saved[0].getSignupCodeAttempts());
        assertEquals(Boolean.FALSE, saved[0].getEmailVerified());

        AuthResponse response = authService.verifySignupCode("leader@example.com", code);

        assertNull(response.getToken());
        assertFalse(response.isTwoFactorRequired());
        assertEquals(Boolean.TRUE, saved[0].getEmailVerified());
        assertNull(saved[0].getSignupCodeHash());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void expiredSignUpCodeIsRejected() {
        User[] saved = new User[1];
        when(userRepository.save(any())).thenAnswer(invocation -> {
            saved[0] = invocation.getArgument(0);
            return saved[0];
        });
        when(userRepository.findByEmail("leader@example.com")).thenAnswer(invocation ->
                saved[0] == null ? Optional.empty() : Optional.of(saved[0]));
        when(outboundMailService.isConfigured()).thenReturn(true);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");

        authService.register(registerRequest());
        saved[0].setSignupCodeExpiresAt(Instant.EPOCH);

        assertThrows(ResponseStatusException.class, () -> authService.verifySignupCode("leader@example.com", sentCode()));
    }

    @Test
    void unknownEmailDoesNotSendAResetLink() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword("missing@example.com");

        verify(outboundMailService, never()).send(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void resetTokenUpdatesThePasswordAndThenExpires() {
        when(userRepository.findByEmail("leader@example.com")).thenReturn(Optional.of(user));
        when(outboundMailService.isConfigured()).thenReturn(true);
        when(userRepository.findByPasswordResetTokenHash(any())).thenAnswer(invocation -> {
            String hash = invocation.getArgument(0);
            if (hash != null && hash.equals(user.getPasswordResetTokenHash())) {
                return Optional.of(user);
            }
            return Optional.empty();
        });
        when(passwordEncoder.encode("new-password-1")).thenReturn("encoded");

        authService.forgotPassword("leader@example.com");
        String token = sentResetToken();

        authService.resetPassword(token, "new-password-1");

        assertEquals("encoded", user.getPassword());
        assertNull(user.getPasswordResetTokenHash());

        user.setPasswordResetTokenHash(AuthService.sha256(token));
        user.setPasswordResetExpiresAt(Instant.EPOCH);
        assertThrows(ResponseStatusException.class, () -> authService.resetPassword(token, "new-password-1"));
    }

    /**
     * Confirms that TGG administrators must complete email verification
     * before receiving an authentication token.
     */
    @Test
    void adminLoginRequiresEmailVerificationBeforeIssuingJwt() {
        prepareAdminLogin();

        AuthResponse response = authService.login(loginRequest());

        assertTrue(response.isTwoFactorRequired());
        assertNull(response.getToken());
        assertEquals("TGG_ADMIN", response.getRole());
        assertTrue(response.getChallengeToken().matches("[0-9a-f]{64}"));

        verify(jwtUtil, never()).generateToken(any());
        verify(outboundMailService).send(
                eq("leader@example.com"),
                eq("Leadership Compass - Administrator Login Code"),
                anyString(),
                anyString());
    }

    /**
     * Confirms that the correct emailed code completes administrator login,
     * while the same challenge cannot be reused.
     */
    @Test
    void correctAdminCodeIssuesJwtAndCannotBeReused() {
        prepareAdminLogin();

        AuthResponse pending = authService.login(loginRequest());

        ArgumentCaptor<AdminLoginChallenge> challengeCaptor =
                ArgumentCaptor.forClass(AdminLoginChallenge.class);

        verify(adminLoginChallengeRepository).save(challengeCaptor.capture());

        AdminLoginChallenge challenge = challengeCaptor.getValue();
        String code = sentAdminCode();

        when(adminLoginChallengeRepository.findWithLockByChallengeTokenHash(
                AuthService.sha256(pending.getChallengeToken())))
                .thenReturn(Optional.of(challenge));

        when(jwtUtil.generateToken(user)).thenReturn("admin-jwt");

        AuthResponse verified = authService.verifyAdminLoginCode(
                pending.getChallengeToken(), code);

        assertEquals("admin-jwt", verified.getToken());
        assertFalse(verified.isTwoFactorRequired());
        assertTrue(challenge.getUsedAt() != null);

        assertThrows(ResponseStatusException.class, () ->
                authService.verifyAdminLoginCode(
                        pending.getChallengeToken(), code));
    }

    /**
     * Confirms that incorrect administrator codes are counted and that
     * five incorrect attempts permanently invalidate the challenge.
     */
    @Test
    void wrongAdminCodesAreLimitedToFiveAttempts() {
        prepareAdminLogin();

        AuthResponse pending = authService.login(loginRequest());

        ArgumentCaptor<AdminLoginChallenge> challengeCaptor =
                ArgumentCaptor.forClass(AdminLoginChallenge.class);

        verify(adminLoginChallengeRepository).save(challengeCaptor.capture());

        AdminLoginChallenge challenge = challengeCaptor.getValue();

        String correctCode = sentAdminCode();
        String wrongCode = "000000".equals(correctCode) ? "111111" : "000000";

        when(adminLoginChallengeRepository.findWithLockByChallengeTokenHash(
                AuthService.sha256(pending.getChallengeToken())))
                .thenReturn(Optional.of(challenge));

        for (int i = 1; i <= 5; i++) {
            assertThrows(ResponseStatusException.class, () ->
                    authService.verifyAdminLoginCode(
                            pending.getChallengeToken(), wrongCode));

            assertEquals(i, challenge.getAttempts());
        }

        assertTrue(challenge.getUsedAt() != null);

        assertThrows(ResponseStatusException.class, () ->
                authService.verifyAdminLoginCode(
                        pending.getChallengeToken(), correctCode));

        verify(jwtUtil, never()).generateToken(any());
    }

    /**
     * Confirms that expired administrator codes cannot issue a JWT.
     */
    @Test
    void expiredAdminCodeIsRejected() {
        prepareAdminLogin();

        AuthResponse pending = authService.login(loginRequest());

        ArgumentCaptor<AdminLoginChallenge> challengeCaptor =
                ArgumentCaptor.forClass(AdminLoginChallenge.class);

        verify(adminLoginChallengeRepository).save(challengeCaptor.capture());

        AdminLoginChallenge challenge = challengeCaptor.getValue();

        challenge.setExpiresAt(Instant.EPOCH);

        when(adminLoginChallengeRepository.findWithLockByChallengeTokenHash(
                AuthService.sha256(pending.getChallengeToken())))
                .thenReturn(Optional.of(challenge));

        assertThrows(ResponseStatusException.class, () ->
                authService.verifyAdminLoginCode(
                        pending.getChallengeToken(), sentAdminCode()));

        verify(jwtUtil, never()).generateToken(any());
    }

    /**
     * Confirms that administrator login fails closed if email is unavailable.
     */
    @Test
    void adminLoginRejectsUnavailableMailService() {
        user.setRole(Role.TGG_ADMIN);
        user.setEmailVerified(true);

        when(userRepository.findByEmail("leader@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "stored")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                authService.login(loginRequest()));

        verify(jwtUtil, never()).generateToken(any());
    }

    /**
     * Confirms that too many administrator verification requests are rejected.
     */
    @Test
    void adminLoginRateLimitRejectsExcessiveRequests() {
        prepareAdminLogin();

        when(adminLoginChallengeRepository.countByUserIdAndCreatedAtAfter(
                any(), any())).thenReturn(3L);

        assertThrows(ResponseStatusException.class, () ->
                authService.login(loginRequest()));

        verify(outboundMailService, never()).send(
                anyString(), anyString(), anyString(), anyString());

        verify(jwtUtil, never()).generateToken(any());
    }

    /**
     * Confirms that failed email delivery does not return a challenge.
     */
    @Test
    void adminLoginRejectsFailedEmailDelivery() {
        prepareAdminLogin();

        doThrow(new IllegalStateException("SMTP unavailable"))
                .when(outboundMailService)
                .send(anyString(), anyString(), anyString(), anyString());

        assertThrows(ResponseStatusException.class, () ->
                authService.login(loginRequest()));

        verify(jwtUtil, never()).generateToken(any());
    }

    /**
     * Configures an authenticated TGG administrator for verification tests.
     */
    private void prepareAdminLogin() {
        user.setId(1L);
        user.setRole(Role.TGG_ADMIN);
        user.setEmailVerified(true);

        lenient().when(userRepository.findByEmail("leader@example.com"))
                .thenReturn(Optional.of(user));
        lenient().when(passwordEncoder.matches("secret", "stored"))
                .thenReturn(true);
        lenient().when(outboundMailService.isConfigured()).thenReturn(true);
    }

    /**
     * Extracts the six-digit code from the administrator email.
     */
    private String sentAdminCode() {
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);

        verify(outboundMailService).send(
                eq("leader@example.com"),
                eq("Leadership Compass - Administrator Login Code"),
                text.capture(),
                anyString());

        Matcher matcher = Pattern.compile("\\d{6}").matcher(text.getValue());
        assertTrue(matcher.find());

        return matcher.group();
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Leader");
        request.setEmail("leader@example.com");
        request.setPassword("secret");
        request.setDepartment("People");
        request.setOrganisation("TGG");
        return request;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("leader@example.com");
        request.setPassword("secret");
        return request;
    }

    private String sentCode() {
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(outboundMailService).send(
                eq("leader@example.com"),
                eq("Confirm your Leadership Compass account"),
                text.capture(),
                anyString());
        Matcher matcher = Pattern.compile("\\d{6}").matcher(text.getValue());
        assertTrue(matcher.find());
        return matcher.group();
    }

    private String sentResetToken() {
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(outboundMailService).send(
                eq("leader@example.com"),
                eq("Reset your Leadership Compass password"),
                text.capture(),
                anyString());
        Matcher matcher = Pattern.compile("token=([^\\s]+)").matcher(text.getValue());
        assertTrue(matcher.find());
        return matcher.group(1);
    }
}
