package com.example.leadershipcompass_capstoneprojectbackend.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String token;
    private String password;
}
