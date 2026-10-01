package com.example.leadershipcompass_capstoneprojectbackend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.leadershipcompass_capstoneprojectbackend.model.Role;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * A missing plan must stay a 404 so the 5-week plan page can show its empty state.
 * Spring Security otherwise replaces that 404 with 403 when the error dispatch is denied.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DevelopmentPlanCurrentSecurityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void missingPlanIsNotFoundInsteadOfForbidden() throws Exception {
        User user = userRepository.save(User.builder()
                .fullName("No Plan")
                .email("no-plan-" + UUID.randomUUID() + "@example.com")
                .password("password123")
                .role(Role.USER)
                .build());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/development-plans/current"))
                .header("Authorization", "Bearer " + jwtUtil.generateToken(user))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}
