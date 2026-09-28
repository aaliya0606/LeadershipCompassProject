package com.example.leadershipcompass_capstoneprojectbackend.model;

/**
 * Outcome of an outbound email attempt.
 */
public enum EmailSendStatus {
    /** Provider accepted the message. */
    SENT,
    /** Send was attempted and failed. */
    FAILED
}
