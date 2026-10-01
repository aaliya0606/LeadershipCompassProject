package com.example.leadershipcompass_capstoneprojectbackend.model;

/**
 * Kind of outbound plan email recorded in the send ledger.
 */
public enum EmailSendKind {
    /** Welcome email sent when a plan is generated. */
    WELCOME,
    /** Reminder sent at the start of weeks 2–5. */
    WEEK_START
}
