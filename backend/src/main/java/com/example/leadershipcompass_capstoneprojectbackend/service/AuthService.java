package com.example.leadershipcompass_capstoneprojectbackend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AuthResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.LoginRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.RegisterRequest;
import com.example.leadershipcompass_capstoneprojectbackend.model.Role;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import com.example.leadershipcompass_capstoneprojectbackend.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                // Public registration must never grant administrator privileges.
                .role(Role.USER)
                .department(request.getDepartment())
                .organisation(request.getOrganisation() == null
                ? null
                : request.getOrganisation().trim())
                .build();

        userRepository.save(user);

        // token = jwtUtil.generateToken(user);
        return new AuthResponse(
                null,
                user.getRole().name(),
                "Successfully created a new user with role" + user.getRole(),
                null);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);
        String preview = token.substring(0, 30) + "...";
        return new AuthResponse(
                token,
                user.getRole().name(),
                "Successfully logged in",
                preview);
    }
}