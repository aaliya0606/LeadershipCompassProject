package com.example.leadershipcompass_capstoneprojectbackend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AuthResponse {

    private String token;
    private String role;
    private String message;
    private String tokenPreview;
}
