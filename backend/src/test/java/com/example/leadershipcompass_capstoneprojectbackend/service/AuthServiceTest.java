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

    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtUtil,
                outboundMailService,
                "https://leadership.example");
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
