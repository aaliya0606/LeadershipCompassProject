package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private String department;

    @Column
    private String organisation;

    /**
     * When true, plan emails are not sent. Null is treated as still subscribed
     * so existing rows stay opted in when the column is added.
     */
    @Column(name = "email_opt_out")
    private Boolean emailOptOut;

    /** Secret used by the unsubscribe link. Null until the first email is prepared. */
    @Column(name = "unsubscribe_token", unique = true, length = 64)
    private String unsubscribeToken;
}
