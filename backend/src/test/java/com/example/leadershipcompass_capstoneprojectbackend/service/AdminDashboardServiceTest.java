package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.repository.SurveyResultRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

/**
 * Tests Admin Dashboard aggregation, department privacy thresholds,
 * skill gap identification, and recommended focus generation.
 */
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SurveyResultRepository surveyResultRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void shouldReturnThreeLowestScoringLeadershipAreasAsSkillGaps() {

        User user = User.builder()
        .id(1L)
        .build();

        // Arrange
        SurveyResult result = SurveyResult.builder()
                .user(user)
                .caringTimeScore(32)
                .receivingValueScore(30)
                .actsOfSupportScore(27)
                .wordsOfRecognitionScore(19)
                .psychologicalTouchScore(33)
                .overallScore(141)
                .build();

        when(userRepository.count()).thenReturn(10L);
        when(surveyResultRepository.findAll())
                .thenReturn(List.of(result));

        // Act
        AdminDashboardResponse response =
                adminDashboardService.getDashboardData("all");

        // Assert
        assertEquals(3, response.getSkillGaps().size());

        assertEquals(
                List.of(
                        "Words of Recognition",
                        "Acts of Support",
                        "Receiving Value"
                ),
                List.copyOf(response.getSkillGaps().keySet())
        );

        assertEquals(19.0, response.getSkillGaps().get("Words of Recognition"));
        assertEquals(27.0, response.getSkillGaps().get("Acts of Support"));
        assertEquals(30.0, response.getSkillGaps().get("Receiving Value"));
    }

    @Test
    void shouldReturnRecommendedFocusForSkillGaps() {
        User user = User.builder()
        .id(1L)
        .build();

        // Arrange
        SurveyResult result = SurveyResult.builder()
                .user(user)
                .caringTimeScore(32)
                .receivingValueScore(30)
                .actsOfSupportScore(27)
                .wordsOfRecognitionScore(19)
                .psychologicalTouchScore(33)
                .overallScore(141)
                .build();

        when(userRepository.count()).thenReturn(10L);
        when(surveyResultRepository.findAll())
                .thenReturn(List.of(result));

        // Act
        AdminDashboardResponse response =
                adminDashboardService.getDashboardData("all");

        // Assert
        assertEquals(
                List.of(
                        "Prioritise timely and specific recognition of team contributions.",
                        "Focus on practical support behaviours and removing barriers that affect team performance.",
                        "Strengthen active listening practices and follow-up on team feedback."
                ),
                response.getRecommendedFocus()
        );
    }
}