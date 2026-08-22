package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.ProgressEntryResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.PeerComparisonResponse;
import com.example.leadershipcompass_capstoneprojectbackend.model.Resource;
import com.example.leadershipcompass_capstoneprojectbackend.service.SurveyService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DashboardController {

    private final SurveyService surveyService;

    public DashboardController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping("/api/dashboard/user")
    public List<ProgressEntryResponse> userDashboard(Authentication authentication) {
        String email = authentication.getName();
        return surveyService.getProgressOverTime(email);
    }

    @GetMapping("/api/dashboard/admin")
    public String adminDashboard() {
        return "Welcome ADMIN. You have access to the admin dashboard.";
    }

    @GetMapping("/api/dashboard/peer-comparison")
    public PeerComparisonResponse peerComparison(Authentication authentication){
        String email = authentication.getName();
        return surveyService.getPeerComparison(email);
    }

    @GetMapping("/api/dashboard/suggested-modules")
    public List<Resource> suggestedModules(Authentication authentication) {
        String email = authentication.getName();
        return surveyService.getSuggestedLearningPath(email);
    }
}