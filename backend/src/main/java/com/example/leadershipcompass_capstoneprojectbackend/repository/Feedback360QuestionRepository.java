package com.example.leadershipcompass_capstoneprojectbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Question;

public interface Feedback360QuestionRepository
        extends JpaRepository<Feedback360Question, Long> {

    List<Feedback360Question> findAllByOrderByQuestionNumberAsc();

    Optional<Feedback360Question> findByQuestionNumber(Integer questionNumber);
}