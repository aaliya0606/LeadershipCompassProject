package com.example.leadershipcompass_capstoneprojectbackend.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.mock.env.MockEnvironment;

class ConfiguredMailConditionTest {

    private final ConfiguredMailCondition condition = new ConfiguredMailCondition();

    @Test
    void isFalseUntilMailIsEnabledWithCredentials() {
        assertFalse(condition.matches(context(new MockEnvironment()), null));

        MockEnvironment smtpWithoutPassword = new MockEnvironment()
                .withProperty("app.mail.enabled", "true")
                .withProperty("app.mail.provider", "smtp")
                .withProperty("spring.mail.username", "no-reply@theguineagroup.com.au");
        assertFalse(condition.matches(context(smtpWithoutPassword), null));

        MockEnvironment smtpReady = new MockEnvironment()
                .withProperty("app.mail.enabled", "true")
                .withProperty("app.mail.provider", "smtp")
                .withProperty("spring.mail.username", "no-reply@theguineagroup.com.au")
                .withProperty("spring.mail.password", "app-password");
        assertTrue(condition.matches(context(smtpReady), null));

        MockEnvironment graphReady = new MockEnvironment()
                .withProperty("app.mail.enabled", "true")
                .withProperty("app.mail.provider", "graph")
                .withProperty("app.mail.graph.tenant-id", "tenant")
                .withProperty("app.mail.graph.client-id", "client")
                .withProperty("app.mail.graph.client-secret", "secret")
                .withProperty("app.mail.graph.sender", "no-reply@theguineagroup.com.au");
        assertTrue(condition.matches(context(graphReady), null));
    }

    private static ConditionContext context(MockEnvironment environment) {
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);
        return context;
    }
}
