package com.example.leadershipcompass_capstoneprojectbackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.leadershipcompass_capstoneprojectbackend.dto.DevelopmentPlanDto;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlan;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlanAction;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlanWeek;
import com.example.leadershipcompass_capstoneprojectbackend.model.Role;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.DevelopmentPlanRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.ModulesRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.SurveyResultRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for checking and unchecking development-plan actions.
 */
@ExtendWith(MockitoExtension.class)
class DevelopmentPlanServiceToggleActionTest {

    @Mock
    private DevelopmentPlanRepository developmentPlanRepository;

    @Mock
    private SurveyResultRepository surveyResultRepository;

    @Mock
    private ModulesRepository modulesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AiBrainService aiBrainService;

    private DevelopmentPlanService developmentPlanService;

    @BeforeEach
    void setUp() {
        developmentPlanService = new DevelopmentPlanService(
                developmentPlanRepository,
                surveyResultRepository,
                modulesRepository,
                userRepository,
                objectMapper,
                aiBrainService);
    }

    @Test
    void shouldMarkActionCompleteAndRecordTimestamp() {
        User user = testUser();
        DevelopmentPlan plan = testPlan(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(developmentPlanRepository.findByIdAndUserId(10L, user.getId())).thenReturn(Optional.of(plan));
        when(developmentPlanRepository.save(plan)).thenReturn(plan);

        DevelopmentPlanDto result = developmentPlanService.toggleAction(user.getEmail(), 10L, 1, 0, true);

        assertTrue(plan.getWeeks().get(0).getActions().get(0).isCompleted());
        assertNotNull(plan.getWeeks().get(0).getActions().get(0).getCompletedAt());
        assertTrue(result.getWeeks().get(0).getActions().get(0).isCompleted());
        verify(developmentPlanRepository).save(plan);
    }

    @Test
    void shouldClearTimestampWhenActionIsUnchecked() {
        User user = testUser();
        DevelopmentPlan plan = testPlan(user);
        DevelopmentPlanAction completedAction = new DevelopmentPlanAction();
        completedAction.setText("Practice one behaviour this week.");
        completedAction.setCompleted(true);
        completedAction.setCompletedAt(Instant.parse("2026-08-01T00:00:00Z"));
        plan.getWeeks().get(0).getActions().set(0, completedAction);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(developmentPlanRepository.findByIdAndUserId(10L, user.getId())).thenReturn(Optional.of(plan));
        when(developmentPlanRepository.save(plan)).thenReturn(plan);

        DevelopmentPlanDto result = developmentPlanService.toggleAction(user.getEmail(), 10L, 1, 0, false);

        assertFalse(plan.getWeeks().get(0).getActions().get(0).isCompleted());
        assertNull(plan.getWeeks().get(0).getActions().get(0).getCompletedAt());
        assertFalse(result.getWeeks().get(0).getActions().get(0).isCompleted());
    }

    @Test
    void shouldRejectToggleWhenPlanIsNotOwnedByUser() {
        User user = testUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(developmentPlanRepository.findByIdAndUserId(10L, user.getId())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> developmentPlanService.toggleAction(user.getEmail(), 10L, 1, 0, true));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(developmentPlanRepository, never()).save(any());
    }

    @Test
    void shouldRejectToggleWhenWeekIsMissing() {
        User user = testUser();
        DevelopmentPlan plan = testPlan(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(developmentPlanRepository.findByIdAndUserId(10L, user.getId())).thenReturn(Optional.of(plan));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> developmentPlanService.toggleAction(user.getEmail(), 10L, 4, 0, true));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Week not found.", exception.getReason());
    }

    @Test
    void shouldRejectToggleWhenActionIndexIsInvalid() {
        User user = testUser();
        DevelopmentPlan plan = testPlan(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(developmentPlanRepository.findByIdAndUserId(10L, user.getId())).thenReturn(Optional.of(plan));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> developmentPlanService.toggleAction(user.getEmail(), 10L, 1, 9, true));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Action not found.", exception.getReason());
    }

    private User testUser() {
        return User.builder()
                .id(3L)
                .fullName("Alex Leader")
                .email("alex@example.com")
                .password("secret")
                .role(Role.USER)
                .build();
    }

    private DevelopmentPlan testPlan(User user) {
        DevelopmentPlan plan = new DevelopmentPlan();
        plan.setId(10L);
        plan.setUser(user);
        plan.setGeneratedAt(Instant.parse("2026-08-20T00:00:00Z"));
        plan.setGenerationSource("RULE_BASED_FALLBACK");
        plan.setCaringTimeScore(20);
        plan.setReceivingValueScore(22);
        plan.setActsOfSupportScore(18);
        plan.setWordsOfRecognitionScore(24);
        plan.setPsychologicalTouchScore(21);

        DevelopmentPlanWeek week = new DevelopmentPlanWeek();
        week.setWeekNumber(1);
        week.setModuleId(8L);
        week.setCategory("Caring Time");
        week.setModuleTitle("Make time for people");
        week.setFocus("Practice presence.");
        week.setRationale("This category needs attention.");
        week.setActions(new ArrayList<>(List.of(
                DevelopmentPlanAction.pending("Practice one behaviour this week."),
                DevelopmentPlanAction.pending("Reflect on a recent conversation."))));
        plan.replaceWeeks(List.of(week));
        return plan;
    }
}
