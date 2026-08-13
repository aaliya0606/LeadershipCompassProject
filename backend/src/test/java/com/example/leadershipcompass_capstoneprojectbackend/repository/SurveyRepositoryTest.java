package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository test class for testing survey-related database operations.
 *
 * <p>This class uses Spring Boot's @DataJpaTest annotation to load only
 * JPA-related components and test repository functionality against the
 * H2 in-memory database configured in the test profile.</p>
 *
 * <p>The tests verify that survey questions, responses, and survey results
 * can be correctly stored and retrieved from the database.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SurveyRepositoryTest {

    /**
     * Injects the UserRepository for creating test users
     * required for survey response and result testing.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Injects the SurveyQuestionRepository for testing
     * survey question database operations.
     */
    @Autowired
    private SurveyQuestionRepository surveyQuestionRepository;

    /**
     * Injects the SurveyResponseRepository for testing
     * survey response database operations.
     */
    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    /**
     * Injects the SurveyResultRepository for testing
     * survey result database operations.
     */
    @Autowired
    private SurveyResultRepository surveyResultRepository;

    /**
     * Tests whether a survey question can be successfully saved.
     *
     * <p>This test verifies that:
     * <ul>
     *     <li>A survey question is stored in the database</li>
     *     <li>An ID is automatically generated</li>
     *     <li>The leadership area is stored correctly</li>
     * </ul>
     * </p>
     */
    @Test
    void shouldSaveSurveyQuestion() {
        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionText("I give my team members my full attention.");
        question.setLeadershipArea("Receiving Value");

        SurveyQuestion savedQuestion = surveyQuestionRepository.save(question);

        assertNotNull(savedQuestion.getId());
        assertEquals("Receiving Value", savedQuestion.getLeadershipArea());
    }

    /**
     * Tests whether a survey response can be successfully saved.
     *
     * <p>This test verifies that:
     * <ul>
     *     <li>A user can submit a survey response</li>
     *     <li>The selected answer value is stored correctly</li>
     *     <li>The associated survey question is linked correctly</li>
     *     <li>The response is linked to the correct user</li>
     * </ul>
     * </p>
     */
    @Test
    void shouldSaveSurveyResponseWithAnswerValueOutOfFive() {
        User user = User.builder()
                .fullName("Test User")
                .email("surveyuser@test.com")
                .password("password123")
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionText("I proactively schedule regular one-on-one meetings.");
        question.setLeadershipArea("Caring Time");

        question = surveyQuestionRepository.save(question);

        SurveyResponse response = new SurveyResponse();
        response.setUser(user);
        response.setQuestion(question);
        response.setAnswerValue(5);

        SurveyResponse savedResponse = surveyResponseRepository.save(response);

        assertNotNull(savedResponse.getId());
        assertEquals(5, savedResponse.getAnswerValue());
        assertEquals("Caring Time", savedResponse.getQuestion().getLeadershipArea());
        assertEquals("surveyuser@test.com", savedResponse.getUser().getEmail());
    }

    /**
     * Tests whether survey result scores can be saved for a user.
     *
     * <p>This test verifies that:
     * <ul>
     *     <li>A survey result record is stored successfully</li>
     *     <li>Each leadership category score is saved correctly</li>
     *     <li>The result is linked to the correct user</li>
     * </ul>
     * </p>
     */
    @Test
    void shouldSaveSurveyResultScoresForUser() {
        User user = User.builder()
                .fullName("Result User")
                .email("resultuser@test.com")
                .password("password123")
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        SurveyResult result = new SurveyResult();
        result.setUser(user);
        result.setCaringTimeScore(45);
        result.setReceivingValueScore(40);
        result.setActsOfSupportScore(38);
        result.setWordsOfRecognitionScore(42);
        result.setPsychologicalTouchScore(35);

        SurveyResult savedResult = surveyResultRepository.save(result);

        assertNotNull(savedResult.getId());
        assertEquals(45, savedResult.getCaringTimeScore());
        assertEquals(40, savedResult.getReceivingValueScore());
        assertEquals(user.getId(), savedResult.getUser().getId());
    }

    /**
     * Tests whether a saved survey question can be retrieved
     * from the database using its generated ID.
     *
     * <p>This test verifies that:
     * <ul>
     *     <li>A survey question is saved successfully</li>
     *     <li>The saved question can be retrieved from the database</li>
     *     <li>The question text remains unchanged</li>
     *     <li>The leadership area remains unchanged</li>
     * </ul>
     * </p>
     */
    @Test
    void shouldSaveAndRetrieveCaringTimeQuestion() {
        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionText(
                "I proactively schedule regular one-on-one meetings with each team member and protect that time from interruptions or cancellations."
        );
        question.setLeadershipArea("Caring Time");

        surveyQuestionRepository.save(question);

        SurveyQuestion retrievedQuestion = surveyQuestionRepository
                .findById(question.getId())
                .orElseThrow();

        assertEquals(
                "I proactively schedule regular one-on-one meetings with each team member and protect that time from interruptions or cancellations.",
                retrievedQuestion.getQuestionText()
        );

        assertEquals("Caring Time", retrievedQuestion.getLeadershipArea());
    }
}