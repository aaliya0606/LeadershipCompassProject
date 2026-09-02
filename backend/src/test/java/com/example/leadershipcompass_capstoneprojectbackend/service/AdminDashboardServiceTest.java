package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.AdminDashboardResponse;
import com.example.leadershipcompass_capstoneprojectbackend.repository.SurveyResultRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SurveyResultRepository surveyResultRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void shouldHideAggregatedResultsWhenDepartmentHasFewerThanSixParticipants() {

        // Arrange: department contains only 5 participants
        when(userRepository.findByDepartment("IT"))
                .thenReturn(Collections.nCopies(5, null));

        when(surveyResultRepository.findByUserDepartment("IT"))
                .thenReturn(Collections.emptyList());

        // Act
        AdminDashboardResponse response =
                adminDashboardService.getDashboardData("IT");

        // Assert
        assertEquals(5, response.getTotalUsers());
        assertEquals(0, response.getAssessmentCompletionRate());
        assertEquals(0, response.getAverageLeadershipScore());
        assertEquals(0, response.getAverageCaringTimeScore());
        assertEquals(0, response.getAverageReceivingValueScore());
        assertEquals(0, response.getAverageActsOfSupportScore());
        assertEquals(0, response.getAverageWordsOfRecognitionScore());
        assertEquals(0, response.getAveragePsychologicalTouchScore());
    }

    @Test
    void shouldAllowAggregatedResultsWhenDepartmentHasSixParticipants() {

        // Arrange: department contains exactly 6 participants
        when(userRepository.findByDepartment("IT"))
                .thenReturn(Collections.nCopies(6, null));

        when(surveyResultRepository.findByUserDepartment("IT"))
                .thenReturn(Collections.emptyList());

        // Act
        AdminDashboardResponse response =
                adminDashboardService.getDashboardData("IT");

        // Assert
        assertEquals(6, response.getTotalUsers());
    }
}