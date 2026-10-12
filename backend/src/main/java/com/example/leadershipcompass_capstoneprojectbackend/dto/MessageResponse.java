package com.example.leadershipcompass_capstoneprojectbackend.dto;

/**
 * Short status text for auth flows that do not return a token.
 *
 * @param message text the page can show
 */
public record MessageResponse(String message) {
}
