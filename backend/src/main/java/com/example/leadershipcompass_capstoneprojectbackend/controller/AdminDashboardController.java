package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.example.leadershipcompass_capstoneprojectbackend.service.AdminDashboardService;
import com.example.leadershipcompass_capstoneprojectbackend.service.AdminReportService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides administrator endpoints for aggregated Leadership Compass
 * dashboard data and CSV report exports.
 *
 * Both endpoints support organisation-wide data or department filtering.
 * Access to these endpoints is restricted to administrators through the
 * application's security configuration.
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final AdminReportService adminReportService;

    /**
     * Returns aggregated leadership assessment data for the Admin Dashboard.
     *
     * @param department department to filter by, or "all" for organisation-wide data
     * @return aggregated Admin Dashboard metrics
     */
    @GetMapping
    public AdminDashboardResponse getDashboard(
            @RequestParam(required = false, defaultValue = "all")
            String department) {

        return adminDashboardService.getDashboardData(department);
    }

    /**
     * Downloads aggregated Admin Dashboard metrics as a CSV report.
     *
     * The export uses the same department filtering and privacy rules
     * as the Admin Dashboard.
     *
     * @param department department to export, or "all" for organisation-wide data
     * @return CSV report as a downloadable file
     */
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