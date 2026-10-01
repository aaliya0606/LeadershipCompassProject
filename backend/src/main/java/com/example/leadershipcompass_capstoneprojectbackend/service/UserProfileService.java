package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.UserProfileResponse;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for retrieving account information for the
 * currently authenticated user.
 *
 * <p>User profile data is retrieved using the authenticated user's email
 * address. Only information intended for the User Profile page is mapped
 * to UserProfileResponse so sensitive User entity fields, such as the
 * stored password hash, are never returned to the frontend.</p>
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;

    /**
     * Retrieves profile information for the authenticated user.
     *
     * @param email email obtained from the authenticated user's security context
     * @return safe profile information for the matching user
     * @throws IllegalArgumentException if no user exists for the authenticated email
     */
    public UserProfileResponse getUserProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Authenticated user not found"));

        return new UserProfileResponse(
                user.getFullName(),
                user.getEmail(),
                user.getOrganisation(),
                user.getDepartment(),
                user.getRole()
        );
    }
}