package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AuthResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.LoginRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.RegisterRequest;
import com.example.leadershipcompass_capstoneprojectbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}