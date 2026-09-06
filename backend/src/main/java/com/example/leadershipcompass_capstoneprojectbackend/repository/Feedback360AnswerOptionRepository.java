package com.example.leadershipcompass_capstoneprojectbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360AnswerOption;

public interface Feedback360AnswerOptionRepository
        extends JpaRepository<Feedback360AnswerOption, Long> {
}