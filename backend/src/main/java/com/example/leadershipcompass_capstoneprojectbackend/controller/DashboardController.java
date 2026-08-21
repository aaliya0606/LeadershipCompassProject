// package com.example.leadershipcompass_capstoneprojectbackend.controller;

// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RestController;

/**
 * No sure which one this is for and who commented this.
 * REST controller responsible for handling dashboard-related endpoints.
 *
 * <p>This controller provides separate dashboard access endpoints
 * based on user roles within the Leadership Compass application.</p>
 *
 * <ul>
 *     <li>User dashboard endpoint</li>
 *     <li>Administrator dashboard endpoint</li>
 * </ul>
 *
 * <p>These endpoints can be protected using Spring Security
 * role-based authorization.</p>
 */

//Old controller, hardcoded
// @RestController
// public class DashboardController {

//     /**
//      * Retrieves the dashboard message for standard users.
//      *
//      * @return a welcome message for users with USER access
//      */

//     @GetMapping("/api/dashboard/user")
//     public String userDashboard() {
//         return "Welcome USER. You have access to the user dashboard.";
//     }

//     /**
//      * Retrieves the dashboard message for administrators.
//      *
//      * @return a welcome message for users with ADMIN access
//      */

//     @GetMapping("/api/dashboard/admin")
//     public String adminDashboard() {
//         return "Welcome ADMIN. You have access to the admin dashboard.";
//     }
// }

//new controller that actually takes in data - (Nikki)
package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.ProgressEntryResponse;
import com.example.leadershipcompass_capstoneprojectbackend.service.SurveyService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.leadershipcompass_capstoneprojectbackend.dto.PeerComparisonResponse;

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
}