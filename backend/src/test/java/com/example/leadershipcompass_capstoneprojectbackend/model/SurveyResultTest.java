package com.example.leadershipcompass_capstoneprojectbackend.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurveyResultTest {

    @Test
    void shouldSetAndGetSurveyResultFields() {
        User user = User.builder()
                .id(1L)
                .fullName("Result User")
                .email("result@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        SurveyResult result = new SurveyResult();

        result.setId(1L);
        result.setUser(user);
        result.setCaringTimeScore(45);
        result.setReceivingValueScore(40);
        result.setActsOfSupportScore(38);
        result.setWordsOfRecognitionScore(42);
        result.setPsychologicalTouchScore(35);

        assertEquals(1L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals(45, result.getCaringTimeScore());
        assertEquals(40, result.getReceivingValueScore());
        assertEquals(38, result.getActsOfSupportScore());
        assertEquals(42, result.getWordsOfRecognitionScore());
        assertEquals(35, result.getPsychologicalTouchScore());

        assertEquals("result@example.com", result.getUser().getEmail());
    }

    @Test
    void noArgsConstructorShouldCreateEmptySurveyResult() {
        SurveyResult result = new SurveyResult();

        assertNull(result.getId());
        assertNull(result.getUser());
        assertNull(result.getCaringTimeScore());
        assertNull(result.getReceivingValueScore());
        assertNull(result.getActsOfSupportScore());
        assertNull(result.getWordsOfRecognitionScore());
        assertNull(result.getPsychologicalTouchScore());
    }

    @Test
    void allArgsConstructorShouldSetAllFields() {
        User user = User.builder()
                .id(1L)
                .fullName("Result User")
                .email("result@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        SurveyResult result = new SurveyResult(
                1L,
                user,
                45,
                40,
                38,
                42,
                35
        );

        assertEquals(1L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals(45, result.getCaringTimeScore());
        assertEquals(40, result.getReceivingValueScore());
        assertEquals(38, result.getActsOfSupportScore());
        assertEquals(42, result.getWordsOfRecognitionScore());
        assertEquals(35, result.getPsychologicalTouchScore());
    }
}