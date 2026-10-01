package com.example.leadershipcompass_capstoneprojectbackend.service;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GraphPlanMailTransportTest {

    @Test
    void sendsWithGraphAfterClientCredentialsToken() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GraphPlanMailTransport transport = new GraphPlanMailTransport(
                builder.build(),
                "tenant",
                "client-id",
                "client-secret",
                "no-reply@theguineagroup.com.au");

        server.expect(requestTo("https://login.microsoftonline.com/tenant/oauth2/v2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"access_token\":\"tok\",\"expires_in\":3600}",
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(startsWith("https://graph.microsoft.com/v1.0/users/")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer tok"))
                .andRespond(withStatus(HttpStatus.ACCEPTED));

        transport.send(new PlanMailMessage(
                "no-reply@theguineagroup.com.au",
                "Leadership Compass",
                "theteam@theguineagroup.com.au",
                "leader@example.com",
                "Your plan is ready",
                "Unsubscribe: http://localhost:8080/api/email/unsubscribe?token=abc",
                "<p><a href=\"http://localhost:8080/api/email/unsubscribe?token=abc\">Unsubscribe</a></p>",
                "http://localhost:8080/api/email/unsubscribe?token=abc"));

        server.verify();
    }
}
