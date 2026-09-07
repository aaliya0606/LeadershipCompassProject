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

import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360ActiveSurveyDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360AnswerSubmissionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360OptionResultDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360QuestionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360QuestionOptionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360RatingResultDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360ResultsDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360SubmissionDTO;
import com.example.leadershipcompass_capstoneprojectbackend.dto.Feedback360SurveyHistoryDTO;
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
     * =========================================================
     * CREATE 360 SURVEY
     * =========================================================
     *
     * A leader can only have one active survey at a time.
     *
     * If they already have an active survey that has not expired,
     * return the existing survey instead of generating a new one.
     *
     * New surveys remain active for 30 days.
     */
    public Feedback360Survey createSurvey(String email) {

        User leader =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        /*
         * Look through the leader's previous surveys,
         * newest first.
         */
        List<Feedback360Survey> existingSurveys =
                surveyRepository
                        .findByLeaderIdOrderByCreatedAtDesc(
                                leader.getId()
                        );


        /*
         * If an active, non-expired survey already exists,
         * return it.
         */
        for (Feedback360Survey existingSurvey : existingSurveys) {

            boolean stillActive =
                    existingSurvey.getStatus()
                            == SurveyStatus.ACTIVE
                            &&
                    existingSurvey.getExpiresAt()
                            != null
                            &&
                    LocalDateTime.now()
                            .isBefore(
                                    existingSurvey.getExpiresAt()
                            );


            if (stillActive) {

                return existingSurvey;

            }
        }


        /*
         * No active survey exists.
         *
         * Create a new 30-day survey.
         */
        LocalDateTime now =
                LocalDateTime.now();


        Feedback360Survey survey =
                Feedback360Survey.builder()

                        .leader(leader)

                        .token(
                                UUID.randomUUID()
                                        .toString()
                        )

                        .status(
                                SurveyStatus.ACTIVE
                        )

                        .createdAt(now)

                        .expiresAt(
                                now.plusDays(30)
                        )

                        .build();


        return surveyRepository.save(
                survey
        );
    }


    /*
     * =========================================================
     * GET SURVEY BY TOKEN
     * =========================================================
     *
     * Used by the anonymous reviewer survey page.
     */
    public Feedback360Survey getSurveyByToken(
            String token) {

        return surveyRepository
                .findByToken(token)

                .orElseThrow(() ->
                        new RuntimeException(
                                "360 feedback survey not found"
                        )
                );
    }


    /*
     * =========================================================
     * GET LOGGED-IN USER'S ACTIVE SURVEY
     * =========================================================
     *
     * Used by the user dashboard.
     *
     * Allows the frontend to determine whether the leader
     * already has an active 360 survey when they log in.
     */
    @Transactional(readOnly = true)
    public Feedback360ActiveSurveyDTO getActiveSurveyForUser(
            String email) {

        User leader =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        List<Feedback360Survey> surveys =
                surveyRepository
                        .findByLeaderIdOrderByCreatedAtDesc(
                                leader.getId()
                        );


        /*
         * Look for the newest active,
         * non-expired survey.
         */
        for (Feedback360Survey survey : surveys) {

            boolean active =
                    survey.getStatus()
                            == SurveyStatus.ACTIVE
                            &&
                    survey.getExpiresAt()
                            != null
                            &&
                    LocalDateTime.now()
                            .isBefore(
                                    survey.getExpiresAt()
                            );


            if (active) {

                long responseCount =
                        responseRepository
                                .countBySurveyId(
                                        survey.getId()
                                );


                return new Feedback360ActiveSurveyDTO(

                        true,

                        survey.getId(),

                        survey.getToken(),

                        survey.getStatus()
                                .name(),

                        survey.getCreatedAt(),

                        survey.getExpiresAt(),

                        responseCount
                );
            }
        }


        /*
         * No active survey exists.
         */
        return new Feedback360ActiveSurveyDTO(

                false,

                null,

                null,

                null,

                null,

                null,

                0
        );
    }


    /*
     * =========================================================
     * GET 360 QUESTIONS
     * =========================================================
     *
     * Return all questions and multi-select options
     * for the anonymous reviewer page.
     */
    @Transactional(readOnly = true)
    public List<Feedback360QuestionDTO> getQuestions() {

        List<Feedback360Question> questions =
                questionRepository
                        .findAllByOrderByQuestionNumberAsc();


        return questions.stream()

                .map(question ->

                        new Feedback360QuestionDTO(

                                question.getId(),

                                question.getQuestionNumber(),

                                question.getQuestionText(),

                                question.getQuestionType()
                                        .name(),

                                question.getCategory(),

                                question.getOptions()
                                        .stream()

                                        .map(option ->

                                                new Feedback360QuestionOptionDTO(

                                                        option.getId(),

                                                        option.getOptionText(),

                                                        option.getDisplayOrder()
                                                )
                                        )

                                        .toList()
                        )
                )

                .toList();
    }


    /*
     * =========================================================
     * SUBMIT ANONYMOUS 360 RESPONSE
     * =========================================================
     */
    @Transactional
    public Feedback360Response submitResponse(

            String token,

            Feedback360SubmissionDTO submission) {


        Feedback360Survey survey =
                surveyRepository
                        .findByToken(token)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "360 feedback survey not found"
                                )
                        );


        /*
         * Survey must be active.
         */
        if (survey.getStatus()
                != SurveyStatus.ACTIVE) {

            throw new RuntimeException(
                    "This 360 feedback survey is not active"
            );
        }


        /*
         * Survey must not have passed
         * its 30-day expiry date.
         */
        if (survey.getExpiresAt() != null
                &&
                LocalDateTime.now()
                        .isAfter(
                                survey.getExpiresAt()
                        )) {

            throw new RuntimeException(
                    "This 360 feedback survey has expired"
            );
        }


        /*
         * Submission must contain answers.
         */
        if (submission == null
                ||
                submission.getAnswers() == null
                ||
                submission.getAnswers()
                        .isEmpty()) {

            throw new RuntimeException(
                    "No survey answers were submitted"
            );
        }


        /*
         * Create anonymous response record.
         *
         * No reviewer user ID, email or name
         * is stored.
         */
        Feedback360Response response =
                Feedback360Response.builder()

                        .survey(survey)

                        .submittedAt(
                                LocalDateTime.now()
                        )

                        .build();


        responseRepository.save(
                response
        );


        /*
         * Save every answer.
         */
        for (
                Feedback360AnswerSubmissionDTO submittedAnswer
                : submission.getAnswers()
        ) {


            if (submittedAnswer.getQuestionId()
                    == null) {

                throw new RuntimeException(
                        "Question ID is required"
                );
            }


            Feedback360Question question =
                    questionRepository

                            .findById(
                                    submittedAnswer
                                            .getQuestionId()
                            )

                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "360 question not found"
                                    )
                            );


            Feedback360Answer answer =
                    Feedback360Answer.builder()

                            .response(response)

                            .question(question)

                            .build();


            /*
             * =================================================
             * Q1-Q8
             * Rating questions
             * =================================================
             */
            if (
                    question.getQuestionType()
                            == Feedback360QuestionType.RATING
            ) {

                Integer score =
                        submittedAnswer.getScore();


                if (
                        score == null
                                ||
                        score < 1
                                ||
                        score > 5
                ) {

                    throw new RuntimeException(
                            "Rating must be between 1 and 5"
                    );
                }


                answer.setScore(
                        score
                );
            }


            /*
             * =================================================
             * Q9 + Q12-Q14
             * Written responses
             * =================================================
             */
            else if (
                    question.getQuestionType()
                            == Feedback360QuestionType.TEXT
            ) {

                String text =
                        submittedAnswer
                                .getTextResponse();


                if (
                        text == null
                                ||
                        text.trim()
                                .isEmpty()
                ) {

                    throw new RuntimeException(
                            "Text response cannot be empty"
                    );
                }


                answer.setTextResponse(
                        text.trim()
                );
            }


            /*
             * =================================================
             * Q10-Q11
             * Multi-select
             * =================================================
             */
            else if (
                    question.getQuestionType()
                            == Feedback360QuestionType.MULTI_SELECT
            ) {


                if (
                        submittedAnswer
                                .getSelectedOptionIds()
                                == null
                ) {

                    throw new RuntimeException(
                            "At least one option must be selected"
                    );
                }


                List<Long> selectedOptionIds =
                        submittedAnswer
                                .getSelectedOptionIds()
                                .stream()
                                .distinct()
                                .toList();


                if (
                        selectedOptionIds
                                .isEmpty()
                ) {

                    throw new RuntimeException(
                            "At least one option must be selected"
                    );
                }


                if (
                        selectedOptionIds
                                .size()
                                > 3
                ) {

                    throw new RuntimeException(
                            "A maximum of 3 options can be selected"
                    );
                }
            }


            /*
             * Save main answer first.
             */
            answerRepository.save(
                    answer
            );


            /*
             * Save selected Q10/Q11 options.
             */
            if (
                    question.getQuestionType()
                            == Feedback360QuestionType.MULTI_SELECT
            ) {


                List<Long> selectedOptionIds =
                        submittedAnswer
                                .getSelectedOptionIds()
                                .stream()
                                .distinct()
                                .toList();


                for (
                        Long optionId
                        : selectedOptionIds
                ) {


                    Feedback360QuestionOption option =
                            questionOptionRepository

                                    .findById(optionId)

                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "Question option not found"
                                            )
                                    );


                    /*
                     * Make sure an option cannot be
                     * submitted against another question.
                     */
                    if (
                            !option.getQuestion()
                                    .getId()
                                    .equals(
                                            question.getId()
                                    )
                    ) {

                        throw new RuntimeException(
                                "Selected option does not belong to this question"
                        );
                    }


                    Feedback360AnswerOption answerOption =
                            Feedback360AnswerOption.builder()

                                    .answer(answer)

                                    .option(option)

                                    .build();


                    answerOptionRepository.save(
                            answerOption
                    );
                }
            }
        }


        return response;
    }


    /*
     * =========================================================
     * GET AGGREGATED 360 RESULTS
     * =========================================================
     */
    @Transactional(readOnly = true)
    public Feedback360ResultsDTO getResults(
            Long surveyId) {


        surveyRepository
                .findById(surveyId)

                .orElseThrow(() ->
                        new RuntimeException(
                                "360 feedback survey not found"
                        )
                );


        long responseCount =
                responseRepository
                        .countBySurveyId(
                                surveyId
                        );


        /*
         * Anonymity threshold can be enabled later.
         *
         * Example:
         *
         * if (responseCount < 3) {
         *
         *     throw new RuntimeException(
         *         "At least 3 responses are required to view results"
         *     );
         * }
         */


        List<Feedback360Answer> answers =
                answerRepository
                        .findByResponseSurveyId(
                                surveyId
                        );


        /*
         * =================================================
         * Q1-Q8
         * Average ratings
         * =================================================
         */

        List<Feedback360RatingResultDTO> ratings =
                new ArrayList<>();


        Map<
                Feedback360Question,
                List<Feedback360Answer>
                >
                ratingGroups =

                answers.stream()

                        .filter(answer ->
                                answer.getScore()
                                        != null
                        )

                        .collect(
                                Collectors.groupingBy(
                                        Feedback360Answer::getQuestion
                                )
                        );


        ratingGroups.forEach(
                (question, questionAnswers) -> {


                    double average =
                            questionAnswers
                                    .stream()

                                    .mapToInt(
                                            Feedback360Answer::getScore
                                    )

                                    .average()

                                    .orElse(
                                            0.0
                                    );


                    /*
                     * Round to two decimal places.
                     */
                    average =
                            Math.round(
                                    average
                                            * 100.0
                            )
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


        /*
         * Keep questions in Q1-Q8 order.
         */
        ratings.sort(
                (a, b) ->

                        Integer.compare(

                                a.getQuestionNumber(),

                                b.getQuestionNumber()
                        )
        );


        /*
         * =================================================
         * Q10-Q11
         * Aggregate selected options
         * =================================================
         */

        List<Feedback360AnswerOption> answerOptions =
                answerOptionRepository
                        .findByAnswerResponseSurveyId(
                                surveyId
                        );


        Map<
                Integer,
                Map<String, Long>
                >
                groupedOptions =

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


        Map<
                Integer,
                List<Feedback360OptionResultDTO>
                >
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
         * Q9 + Q12-Q14
         * Anonymous written feedback
         * =================================================
         */

        Map<
                Integer,
                List<String>
                >
                writtenFeedback =

                answers.stream()

                        .filter(answer ->

                                answer.getTextResponse()
                                        != null

                                        &&

                                !answer.getTextResponse()
                                        .isBlank()
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
     * =========================================================
     * RESPONSE COUNT
     * =========================================================
     */
    public long getResponseCount(
            Long surveyId) {

        return responseRepository
                .countBySurveyId(
                        surveyId
                );
    }
    @Transactional(readOnly = true)
public List<Feedback360SurveyHistoryDTO> getSurveyHistory(
        String email) {

    User leader =
            userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found"
                            )
                    );

    List<Feedback360Survey> surveys =
            surveyRepository
                    .findByLeaderIdOrderByCreatedAtDesc(
                            leader.getId()
                    );

    return surveys.stream()
            .map(survey ->
                    new Feedback360SurveyHistoryDTO(
                            survey.getId(),
                            survey.getStatus().name(),
                            survey.getCreatedAt(),
                            survey.getExpiresAt(),
                            responseRepository
                                    .countBySurveyId(
                                            survey.getId()
                                    )
                    )
            )
            .toList();
}
}