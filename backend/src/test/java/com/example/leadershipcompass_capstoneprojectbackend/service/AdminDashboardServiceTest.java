package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlan;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlanAction;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlanWeek;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.repository.SurveyResultRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.DevelopmentPlanRepository;

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
    @Mock
    private DevelopmentPlanRepository developmentPlanRepository;

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

        // Return the users included in the dashboard aggregation.
        when(userRepository.findAll()).thenReturn(List.of(user));
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

        // Return the users included in the dashboard aggregation.
        when(userRepository.findAll()).thenReturn(List.of(user));
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

    @Test
        void shouldCalculateDevelopmentPlanCompletionRate() {

        // Arrange
        User user = User.builder()
                .id(1L)
                .build();

        DevelopmentPlanAction completedAction =
                DevelopmentPlanAction.pending("Completed action");
        completedAction.setCompleted(true);

        DevelopmentPlanAction pendingAction =
                DevelopmentPlanAction.pending("Pending action");

        DevelopmentPlanWeek week = new DevelopmentPlanWeek();
        week.getActions().add(completedAction);
        week.getActions().add(pendingAction);

        DevelopmentPlan plan = new DevelopmentPlan();
        plan.getWeeks().add(week);

        when(userRepository.findAll())
                .thenReturn(List.of(user));

        when(surveyResultRepository.findAll())
                .thenReturn(Collections.emptyList());

        when(developmentPlanRepository.findFirstByUserIdOrderByGeneratedAtDesc(1L))
                .thenReturn(java.util.Optional.of(plan));

        // Act
        AdminDashboardResponse response =
                adminDashboardService.getDashboardData("all");

        // Assert
        assertEquals(
                50.0,
                response.getDevelopmentPlanCompletionRate(),
                0.01
        );
    }
}