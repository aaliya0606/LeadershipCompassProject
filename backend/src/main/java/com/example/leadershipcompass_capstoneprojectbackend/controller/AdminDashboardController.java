package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.example.leadershipcompass_capstoneprojectbackend.service.AdminDashboardService;
import com.example.leadershipcompass_capstoneprojectbackend.service.AdminReportService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;



@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor

public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    private final AdminReportService adminReportService;

    @GetMapping
    public AdminDashboardResponse getDashboard(
            @RequestParam(required = false, defaultValue = "all")
            String department) {

        return adminDashboardService.getDashboardData(department);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportDashboard(
            @RequestParam(required = false, defaultValue = "all")
            String department) {

        String csv = adminReportService.generateCsvReport(department);

        String filename = department.equalsIgnoreCase("all")
                ? "leadership-compass-report-all.csv"
                : "leadership-compass-report-" + department + ".csv";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\""
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}