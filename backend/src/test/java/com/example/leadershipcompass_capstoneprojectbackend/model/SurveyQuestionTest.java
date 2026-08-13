package com.example.leadershipcompass_capstoneprojectbackend.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurveyQuestionTest {

    @Test
    void shouldSetAndGetSurveyQuestionFields() {
        SurveyQuestion question = new SurveyQuestion();

        question.setId(1L);
        question.setQuestionText("I give my team members my full attention.");
        question.setLeadershipArea("Receiving Value");

        assertEquals(1L, question.getId());
        assertEquals("I give my team members my full attention.", question.getQuestionText());
        assertEquals("Receiving Value", question.getLeadershipArea());
    }

    @Test
    void noArgsConstructorShouldCreateEmptySurveyQuestion() {
        SurveyQuestion question = new SurveyQuestion();

        assertNull(question.getId());
        assertNull(question.getQuestionText());
        assertNull(question.getLeadershipArea());
    }

    @Test
    void allArgsConstructorShouldSetAllFields() {
        SurveyQuestion question = new SurveyQuestion(
                1L,
                "I proactively support my team.",
                "Acts of Support"
        );

        assertEquals(1L, question.getId());
        assertEquals("I proactively support my team.", question.getQuestionText());
        assertEquals("Acts of Support", question.getLeadershipArea());
    }
}