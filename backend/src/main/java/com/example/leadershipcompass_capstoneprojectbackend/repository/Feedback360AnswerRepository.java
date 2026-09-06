package com.example.leadershipcompass_capstoneprojectbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Answer;

public interface Feedback360AnswerRepository
        extends JpaRepository<Feedback360Answer, Long> {
}