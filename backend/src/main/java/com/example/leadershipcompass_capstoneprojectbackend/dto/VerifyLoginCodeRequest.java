package com.example.leadershipcompass_capstoneprojectbackend.dto;

import lombok.Data;

@Data
public class VerifyLoginCodeRequest {

    private String email;
    private String code;
}
