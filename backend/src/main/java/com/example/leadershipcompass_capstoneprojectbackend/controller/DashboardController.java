package com.example.leadershipcompass_capstoneprojectbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    @GetMapping("/api/dashboard/user")
    public String userDashboard() {
        return "Welcome USER. You have access to the user dashboard.";
    }

    @GetMapping("/api/dashboard/admin")
    public String adminDashboard() {
        return "Welcome ADMIN. You have access to the admin dashboard.";
    }
}