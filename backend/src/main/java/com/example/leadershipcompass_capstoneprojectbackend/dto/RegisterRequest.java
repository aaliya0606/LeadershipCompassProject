package com.example.leadershipcompass_capstoneprojectbackend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class RegisterRequest {

    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
    private String role;    // USER or ADMIN
    private String department;
}
