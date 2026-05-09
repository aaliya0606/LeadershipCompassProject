package com.example.leadershipcompass_capstoneprojectbackend.dto;
import lombok.Data;

@Data

public class RegisterRequest {

    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
    private String role;    // USER or ADMIN
}
