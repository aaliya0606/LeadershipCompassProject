package com.example.leadershipcompass_capstoneprojectbackend.dto;

import com.example.leadershipcompass_capstoneprojectbackend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing the account information required by the
 * User Profile page.
 *
 * <p>This DTO provides a safe representation of the authenticated user's
 * profile without exposing sensitive fields from the User entity, such as
 * the stored password hash.</p>
 *
 * <p>The profile information is retrieved for the currently authenticated
 * user and can be used by the frontend to populate the user's account
 * settings/profile page.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

    /** User's full name. */
    private String fullName;

    /** Email address associated with the user's account. */
    private String email;

    /** Organisation the user belongs to. */
    private String organisation;

    /** Department within the user's organisation. */
    private String department;

    /** Application role assigned to the user, such as USER or ADMIN. */
    private Role role;
}