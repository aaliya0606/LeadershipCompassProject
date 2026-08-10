package com.example.leadershipcompass_capstoneprojectbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
public class DashboardController {

    /**
     * Retrieves the dashboard message for standard users.
     *
     * @return a welcome message for users with USER access
     */

    @GetMapping("/api/dashboard/user")
    public String userDashboard() {
        return "Welcome USER. You have access to the user dashboard.";
    }

    /**
     * Retrieves the dashboard message for administrators.
     *
     * @return a welcome message for users with ADMIN access
     */

    @GetMapping("/api/dashboard/admin")
    public String adminDashboard() {
        return "Welcome ADMIN. You have access to the admin dashboard.";
    }
}

