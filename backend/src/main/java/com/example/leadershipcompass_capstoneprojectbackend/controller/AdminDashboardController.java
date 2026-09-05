package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.example.leadershipcompass_capstoneprojectbackend.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public AdminDashboardResponse getDashboard(
            @RequestParam(required = false, defaultValue = "all")
            String department) {

        return adminDashboardService.getDashboardData(department);
    }
}