package com.example.leadershipcompass_capstoneprojectbackend.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurveyResponseTest {

    @Test
    void shouldSetAndGetSurveyResponseFields() {
        User user = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        SurveyQuestion question = new SurveyQuestion();
        question.setId(1L);
        question.setQuestionText("I listen carefully to others.");
        question.setLeadershipArea("Receiving Value");

        SurveyResponse response = new SurveyResponse();

        response.setId(1L);
        response.setUser(user);
        response.setQuestion(question);
        response.setAnswerValue(5);

        assertEquals(1L, response.getId());
        assertEquals(user, response.getUser());
        assertEquals(question, response.getQuestion());
        assertEquals(5, response.getAnswerValue());

        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("Receiving Value", response.getQuestion().getLeadershipArea());
    }

    @Test
    void noArgsConstructorShouldCreateEmptySurveyResponse() {
        SurveyResponse response = new SurveyResponse();

        assertNull(response.getId());
        assertNull(response.getUser());
        assertNull(response.getQuestion());
        assertNull(response.getAnswerValue());
    }

    @Test
    void allArgsConstructorShouldSetAllFields() {
        User user = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        SurveyQuestion question = new SurveyQuestion(
                1L,
                "I recognise good work.",
                "Words of Recognition"
        );

        SurveyResponse response = new SurveyResponse(
                1L,
                user,
                question,
                4
        );

        assertEquals(1L, response.getId());
        assertEquals(user, response.getUser());
        assertEquals(question, response.getQuestion());
        assertEquals(4, response.getAnswerValue());
    }
}