package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.UserProfileResponse;
import com.example.leadershipcompass_capstoneprojectbackend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for User Profile account information.
 *
 * <p>The profile is retrieved for the currently authenticated user.
 * The user's identity is obtained from the Spring Security authentication
 * context rather than being supplied by the frontend.</p>
 *
 * <p>This prevents the frontend from requesting another user's profile
 * by manually supplying a user ID or email address.</p>
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Returns the profile information for the currently authenticated user.
     *
     * <p>The authenticated email is extracted from the user's JWT/security
     * context and is used to retrieve the matching account.</p>
     *
     * @param authentication Spring Security authentication for the current user
     * @return profile information for the authenticated user
     */
    @GetMapping
    public UserProfileResponse getCurrentUserProfile(Authentication authentication) {

        String email = authentication.getName();

        return userProfileService.getUserProfile(email);
    }
}