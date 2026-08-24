package com.example.leadershipcompass_capstoneprojectbackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360QuestionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360QuestionOptionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Question;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Survey;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyStatus;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360QuestionRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360SurveyRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Feedback360Service {

    private final Feedback360SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final Feedback360QuestionRepository questionRepository;

    public Feedback360Survey createSurvey(String email) {

        User leader = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Feedback360Survey survey = Feedback360Survey.builder()
                .leader(leader)
                .token(UUID.randomUUID().toString())
                .status(SurveyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        return surveyRepository.save(survey);
    }

    public Feedback360Survey getSurveyByToken(String token) {

        return surveyRepository.findByToken(token)
                .orElseThrow(() ->
                        new RuntimeException(
                                "360 feedback survey not found"));
    }

    @Transactional(readOnly = true)
    public List<Feedback360QuestionDTO> getQuestions() {

        List<Feedback360Question> questions =
                questionRepository.findAllByOrderByQuestionNumberAsc();

        return questions.stream()
                .map(question -> new Feedback360QuestionDTO(
                        question.getId(),
                        question.getQuestionNumber(),
                        question.getQuestionText(),
                        question.getQuestionType().name(),
                        question.getCategory(),
                        question.getOptions().stream()
                                .map(option -> new Feedback360QuestionOptionDTO(
                                        option.getId(),
                                        option.getOptionText(),
                                        option.getDisplayOrder()
                                ))
                                .toList()
                ))
                .toList();
    }
}