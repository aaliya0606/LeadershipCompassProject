package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import com.example.leadershipcompass_capstoneprojectbackend.service.AdminDashboardService;
import com.example.leadershipcompass_capstoneprojectbackend.service.AdminPdfService;
import com.example.leadershipcompass_capstoneprojectbackend.service.AdminReportService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organisation-admin")
@RequiredArgsConstructor
public class OrganisationAdminController {

    private final UserRepository userRepository;
    private final AdminDashboardService adminDashboardService;
    private final AdminReportService adminReportService;
    private final AdminPdfService adminPdfService;

    /**
     * Returns dashboard data for the authenticated organisation admin's
     * organisation only.
     */
    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "all")
            String department) {

        String organisation = getOrganisation(authentication);

        return adminDashboardService.getDashboardData(
                organisation,
                department
        );
    }

    /**
     * Exports dashboard data as CSV for the authenticated organisation
     * admin's organisation only.
     */
    @GetMapping("/dashboard/export")
    public ResponseEntity<String> exportCsv(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "all")
            String department) {

        String organisation = getOrganisation(authentication);

        String csv = adminReportService.generateCsvReport(
                organisation,
                department
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"leadership-compass-organisation-report.csv\""
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /**
     * Exports dashboard data as PDF for the authenticated organisation
     * admin's organisation only.
     */
    @GetMapping("/dashboard/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "all")
            String department) {

        String organisation = getOrganisation(authentication);

        byte[] pdf = adminPdfService.generatePdfReport(
                organisation,
                department
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"leadership-compass-organisation-report.pdf\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Gets the organisation assigned to the currently authenticated
     * organisation administrator.
     */
    private String getOrganisation(Authentication authentication) {

        User admin = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found")
                );

        String organisation = admin.getOrganisation();

        if (organisation == null || organisation.isBlank()) {
            throw new IllegalStateException(
                    "Organisation administrator is not assigned to an organisation"
            );
        }

        return organisation;
    }
}
