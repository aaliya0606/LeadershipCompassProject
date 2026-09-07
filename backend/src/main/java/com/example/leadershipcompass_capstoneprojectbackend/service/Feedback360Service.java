package com.example.leadershipcompass_capstoneprojectbackend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360AnswerSubmissionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360OptionResultDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360QuestionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360QuestionOptionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360RatingResultDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360ResultsDTO;
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

        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new RuntimeException(
                    "This 360 feedback survey is not active");
        }

        if (submission == null
                || submission.getAnswers() == null
                || submission.getAnswers().isEmpty()) {

            throw new RuntimeException(
                    "No survey answers were submitted");
        }

        Feedback360Response response =
                Feedback360Response.builder()
                        .survey(survey)
                        .submittedAt(LocalDateTime.now())
                        .build();

        responseRepository.save(response);

        for (Feedback360AnswerSubmissionDTO submittedAnswer
                : submission.getAnswers()) {

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
             * Q1-Q8
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
             * Q9 + Q12-Q14
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
             * Q10-Q11
             */
            else if (question.getQuestionType()
                    == Feedback360QuestionType.MULTI_SELECT) {

                if (submittedAnswer.getSelectedOptionIds() == null) {
                    throw new RuntimeException(
                            "At least one option must be selected");
                }

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

            answerRepository.save(answer);

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
     * Return aggregated anonymous 360 results for a survey.
     */
    @Transactional(readOnly = true)
    public Feedback360ResultsDTO getResults(Long surveyId) {

        surveyRepository.findById(surveyId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "360 feedback survey not found"));

        long responseCount =
                responseRepository.countBySurveyId(surveyId);

        /*
         * Later, enable this for anonymity.
         *
         * For testing with your current two responses,
         * leave it commented out.
         */

        /*
        if (responseCount < 3) {
            throw new RuntimeException(
                    "At least 3 responses are required to view results");
        }
        */

        List<Feedback360Answer> answers =
                answerRepository.findByResponseSurveyId(surveyId);

        /*
         * =================================================
         * Q1-Q8: Average ratings
         * =================================================
         */

        List<Feedback360RatingResultDTO> ratings =
                new ArrayList<>();

        Map<Feedback360Question, List<Feedback360Answer>>
                ratingGroups =
                answers.stream()
                        .filter(answer ->
                                answer.getScore() != null)
                        .collect(
                                Collectors.groupingBy(
                                        Feedback360Answer::getQuestion
                                )
                        );

        ratingGroups.forEach(
                (question, questionAnswers) -> {

                    double average =
                            questionAnswers.stream()
                                    .mapToInt(
                                            Feedback360Answer::getScore
                                    )
                                    .average()
                                    .orElse(0.0);

                    /*
                     * Round to 2 decimal places.
                     */
                    average =
                            Math.round(average * 100.0)
                                    / 100.0;

                    ratings.add(
                            new Feedback360RatingResultDTO(
                                    question.getQuestionNumber(),
                                    question.getCategory(),
                                    average
                            )
                    );
                }
        );

        ratings.sort(
                (a, b) ->
                        Integer.compare(
                                a.getQuestionNumber(),
                                b.getQuestionNumber()
                        )
        );

        /*
         * =================================================
         * Q10-Q11: Aggregate selected options
         * =================================================
         */

        List<Feedback360AnswerOption> answerOptions =
                answerOptionRepository
                        .findByAnswerResponseSurveyId(
                                surveyId
                        );

        Map<Integer, Map<String, Long>> groupedOptions =
                answerOptions.stream()
                        .collect(
                                Collectors.groupingBy(
                                        answerOption ->
                                                answerOption
                                                        .getAnswer()
                                                        .getQuestion()
                                                        .getQuestionNumber(),

                                        Collectors.groupingBy(
                                                answerOption ->
                                                        answerOption
                                                                .getOption()
                                                                .getOptionText(),

                                                Collectors.counting()
                                        )
                                )
                        );

        Map<Integer, List<Feedback360OptionResultDTO>>
                optionResults =
                new HashMap<>();

        groupedOptions.forEach(
                (questionNumber, counts) -> {

                    List<Feedback360OptionResultDTO> results =
                            counts.entrySet()
                                    .stream()
                                    .map(entry ->
                                            new Feedback360OptionResultDTO(
                                                    entry.getKey(),
                                                    entry.getValue()
                                            )
                                    )
                                    .sorted(
                                            (a, b) ->
                                                    Long.compare(
                                                            b.getCount(),
                                                            a.getCount()
                                                    )
                                    )
                                    .toList();

                    optionResults.put(
                            questionNumber,
                            results
                    );
                }
        );

        /*
         * =================================================
         * Q9 + Q12-Q14: Anonymous written feedback
         * =================================================
         */

        Map<Integer, List<String>> writtenFeedback =
                answers.stream()
                        .filter(answer ->
                                answer.getTextResponse() != null
                                        &&
                                !answer.getTextResponse().isBlank()
                        )
                        .collect(
                                Collectors.groupingBy(
                                        answer ->
                                                answer
                                                        .getQuestion()
                                                        .getQuestionNumber(),

                                        Collectors.mapping(
                                                Feedback360Answer::getTextResponse,
                                                Collectors.toList()
                                        )
                                )
                        );

        return new Feedback360ResultsDTO(
                responseCount,
                ratings,
                optionResults,
                writtenFeedback
        );
    }

    /*
     * Useful for completion tracking and results.
     */
    public long getResponseCount(Long surveyId) {

        return responseRepository
                .countBySurveyId(surveyId);
    }
}