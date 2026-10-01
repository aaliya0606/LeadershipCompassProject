package com.example.leadershipcompass_capstoneprojectbackend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EmailUnsubscribeWebTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Test
    void unsubscribeLinkOptsTheUserOutWithoutLogin() throws Exception {
        String token = "token-" + UUID.randomUUID();
        User user = userRepository.save(User.builder()
                .fullName("Mail User")
                .email("mail-" + UUID.randomUUID() + "@example.com")
                .password("password123")
                .role(Role.USER)
                .unsubscribeToken(token)
                .build());

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/api/email/unsubscribe?token=" + token))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("unsubscribed"));
        assertEquals(Boolean.TRUE, userRepository.findById(user.getId()).orElseThrow().getEmailOptOut());
    }

    @Test
    void oneClickPostUnsubscribes() throws Exception {
        String token = "token-" + UUID.randomUUID();
        User user = userRepository.save(User.builder()
                .fullName("Mail User")
                .email("mail-" + UUID.randomUUID() + "@example.com")
                .password("password123")
                .role(Role.USER)
                .unsubscribeToken(token)
                .build());

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/api/email/unsubscribe?token=" + token))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString("List-Unsubscribe=One-Click"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(Boolean.TRUE, userRepository.findById(user.getId()).orElseThrow().getEmailOptOut());
    }
}
