package com.example.leadershipcompass_capstoneprojectbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360QuestionOption;

public interface Feedback360QuestionOptionRepository
        extends JpaRepository<Feedback360QuestionOption, Long> {

    List<Feedback360QuestionOption> findAllByQuestionIdOrderByDisplayOrderAsc(
            Long questionId);
}