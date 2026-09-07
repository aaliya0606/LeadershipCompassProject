package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

/**
 * Tests generation of admin CSV exports using aggregated dashboard data,
 * including privacy suppression for departments with fewer than six participants.
 */

class AdminReportServiceTest {

    @Mock
    private AdminDashboardService adminDashboardService;

    @InjectMocks
    private AdminReportService adminReportService;

    @Test
    void shouldGenerateCsvReportFromDashboardData() {

        AdminDashboardResponse dashboardResponse =
                new AdminDashboardResponse(
                        14,
                        3,
                        21.43,
                        177.67,
                        38.00,
                        36.67,
                        35.00,
                        29.33,
                        38.67,
                        Map.of(),
                        Map.of(),
                        List.of()
                );

        when(adminDashboardService.getDashboardData("all"))
                .thenReturn(dashboardResponse);

        String csv = adminReportService.generateCsvReport("all");

        assertTrue(csv.contains("Department,all"));
        assertTrue(csv.contains("Total Participants,14"));
        assertTrue(csv.contains("Completed Assessments,3"));
        assertTrue(csv.contains("Assessment Completion Rate,21.43%"));
        assertTrue(csv.contains("Average Leadership Score (out of 250),177.67"));
        assertTrue(csv.contains("Average Caring Time Score (out of 50),38.00"));
        assertTrue(csv.contains("Average Words of Recognition Score (out of 50),29.33"));
    }
    @Test
    void shouldSuppressDepartmentMetricsWhenFewerThanSixParticipants() {

        AdminDashboardResponse dashboardResponse =
                new AdminDashboardResponse(
                        1,
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        Map.of(),
                        Map.of(),
                        List.of()
                );

        when(adminDashboardService.getDashboardData("IT"))
                .thenReturn(dashboardResponse);

        String csv = adminReportService.generateCsvReport("IT");

        assertTrue(csv.contains("Department,IT"));
        assertTrue(csv.contains("Total Participants,1"));
        assertTrue(csv.contains("Completed Assessments,1"));
        assertTrue(csv.contains(
                "Assessment Completion Rate,Suppressed (<6 participants)"
        ));
        assertTrue(csv.contains(
                "Average Leadership Score (out of 250),Suppressed (<6 participants)"
        ));
        assertTrue(csv.contains(
                "Average Caring Time Score (out of 50),Suppressed (<6 participants)"
        ));
    }
}