package com.example.leadershipcompass_capstoneprojectbackend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlan;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlanAction;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlanWeek;
import com.example.leadershipcompass_capstoneprojectbackend.model.Role;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/**
 * Persistence tests for checkable development-plan actions.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DevelopmentPlanActionPersistenceTest {

    @Autowired
    private DevelopmentPlanRepository developmentPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistActionCompletionState() {
        User user = userRepository.save(User.builder()
                .fullName("Alex Leader")
                .email("alex-plan@example.com")
                .password("password123")
                .role(Role.USER)
                .build());

        DevelopmentPlan plan = new DevelopmentPlan();
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
                DevelopmentPlanAction.pending("Practice one behaviour this week."))));
        plan.replaceWeeks(List.of(week));

        DevelopmentPlan saved = developmentPlanRepository.saveAndFlush(plan);
        Long planId = saved.getId();

        DevelopmentPlan reloaded = developmentPlanRepository.findById(planId).orElseThrow();
        DevelopmentPlanAction storedAction = reloaded.getWeeks().get(0).getActions().get(0);
        assertEquals("Practice one behaviour this week.", storedAction.getText());
        assertFalse(storedAction.isCompleted());
        assertNull(storedAction.getCompletedAt());

        DevelopmentPlanAction completedAction = new DevelopmentPlanAction();
        completedAction.setText(storedAction.getText());
        completedAction.setCompleted(true);
        completedAction.setCompletedAt(Instant.parse("2026-08-24T08:00:00Z"));
        reloaded.getWeeks().get(0).getActions().set(0, completedAction);
        developmentPlanRepository.saveAndFlush(reloaded);

        DevelopmentPlan afterToggle = developmentPlanRepository.findById(planId).orElseThrow();
        DevelopmentPlanAction updatedAction = afterToggle.getWeeks().get(0).getActions().get(0);
        assertTrue(updatedAction.isCompleted());
        assertNotNull(updatedAction.getCompletedAt());
        assertEquals("Practice one behaviour this week.", updatedAction.getText());
    }
}
