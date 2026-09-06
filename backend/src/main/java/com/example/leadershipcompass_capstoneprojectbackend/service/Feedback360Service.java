package com.example.leadershipcompass_capstoneprojectbackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360AnswerSubmissionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360QuestionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360QuestionOptionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360SubmissionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Answer;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360AnswerOption;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Question;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360QuestionOption;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360QuestionType;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Response;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Survey;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyStatus;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360AnswerOptionRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360AnswerRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360QuestionOptionRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360QuestionRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360ResponseRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360SurveyRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Feedback360Service {

    private final Feedback360SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final Feedback360QuestionRepository questionRepository;
    private final Feedback360ResponseRepository responseRepository;
    private final Feedback360AnswerRepository answerRepository;
    private final Feedback360QuestionOptionRepository questionOptionRepository;
    private final Feedback360AnswerOptionRepository answerOptionRepository;

    /*
     * Create a new 360 survey for a logged-in leader.
     */
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

    /*
     * Find a 360 survey using the token from the shareable link.
     */
    public Feedback360Survey getSurveyByToken(String token) {

        return surveyRepository.findByToken(token)
                .orElseThrow(() ->
                        new RuntimeException(
                                "360 feedback survey not found"));
    }

    /*
     * Return all 360 questions and multi-select options
     * for the frontend survey page.
     */
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
                                .map(option ->
                                        new Feedback360QuestionOptionDTO(
                                                option.getId(),
                                                option.getOptionText(),
                                                option.getDisplayOrder()
                                        )
                                )
                                .toList()
                ))
                .toList();
    }

    /*
     * Store one anonymous peer's completed 360 response.
     */
    @Transactional
    public Feedback360Response submitResponse(
            String token,
            Feedback360SubmissionDTO submission) {

        Feedback360Survey survey = surveyRepository.findByToken(token)
                .orElseThrow(() ->
                        new RuntimeException(
                                "360 feedback survey not found"));

        /*
         * Only active surveys can accept responses.
         */
        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new RuntimeException(
                    "This 360 feedback survey is not active");
        }

        /*
         * Make sure the request actually contains answers.
         */
        if (submission == null
                || submission.getAnswers() == null
                || submission.getAnswers().isEmpty()) {

            throw new RuntimeException(
                    "No survey answers were submitted");
        }

        /*
         * Create one anonymous response record.
         * No reviewer name, email or user ID is stored.
         */
        Feedback360Response response =
                Feedback360Response.builder()
                        .survey(survey)
                        .submittedAt(LocalDateTime.now())
                        .build();

        responseRepository.save(response);

        /*
         * Process each submitted answer.
         */
        for (Feedback360AnswerSubmissionDTO submittedAnswer
                : submission.getAnswers()) {

            /*
             * Every submitted answer must identify its question.
             */
            if (submittedAnswer.getQuestionId() == null) {
                throw new RuntimeException(
                        "Question ID is required");
            }

            Feedback360Question question =
                    questionRepository
                            .findById(submittedAnswer.getQuestionId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "360 question not found"));

            Feedback360Answer answer =
                    Feedback360Answer.builder()
                            .response(response)
                            .question(question)
                            .build();

            /*
             * Q1-Q8:
             * Rating score between 1 and 5.
             */
            if (question.getQuestionType()
                    == Feedback360QuestionType.RATING) {

                Integer score = submittedAnswer.getScore();

                if (score == null
                        || score < 1
                        || score > 5) {

                    throw new RuntimeException(
                            "Rating must be between 1 and 5");
                }

                answer.setScore(score);
            }

            /*
             * Q9 and Q12-Q14:
             * Written response.
             */
            else if (question.getQuestionType()
                    == Feedback360QuestionType.TEXT) {

                String text =
                        submittedAnswer.getTextResponse();

                if (text == null
                        || text.trim().isEmpty()) {

                    throw new RuntimeException(
                            "Text response cannot be empty");
                }

                answer.setTextResponse(text.trim());
            }

            /*
             * Q10-Q11:
             * Multi-select responses.
             */
            else if (question.getQuestionType()
                    == Feedback360QuestionType.MULTI_SELECT) {

                if (submittedAnswer.getSelectedOptionIds() == null) {
                    throw new RuntimeException(
                            "At least one option must be selected");
                }

                /*
                 * Remove duplicate option IDs before validation.
                 */
                List<Long> selectedOptionIds =
                        submittedAnswer
                                .getSelectedOptionIds()
                                .stream()
                                .distinct()
                                .toList();

                if (selectedOptionIds.isEmpty()) {
                    throw new RuntimeException(
                            "At least one option must be selected");
                }

                if (selectedOptionIds.size() > 3) {
                    throw new RuntimeException(
                            "A maximum of 3 options can be selected");
                }
            }

            /*
             * Save the main answer first.
             */
            answerRepository.save(answer);

            /*
             * Save Q10/Q11 selected options.
             */
            if (question.getQuestionType()
                    == Feedback360QuestionType.MULTI_SELECT) {

                List<Long> selectedOptionIds =
                        submittedAnswer
                                .getSelectedOptionIds()
                                .stream()
                                .distinct()
                                .toList();

                for (Long optionId : selectedOptionIds) {

                    Feedback360QuestionOption option =
                            questionOptionRepository
                                    .findById(optionId)
                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "Question option not found"));

                    /*
                     * Prevent an option belonging to another question
                     * being submitted.
                     */
                    if (!option.getQuestion()
                            .getId()
                            .equals(question.getId())) {

                        throw new RuntimeException(
                                "Selected option does not belong to this question");
                    }

                    Feedback360AnswerOption answerOption =
                            Feedback360AnswerOption.builder()
                                    .answer(answer)
                                    .option(option)
                                    .build();

                    answerOptionRepository.save(answerOption);
                }
            }
        }

        return response;
    }

    /*
     * Useful later for completion tracking and results.
     */
    public long getResponseCount(Long surveyId) {
        return responseRepository.countBySurveyId(surveyId);
    }
}