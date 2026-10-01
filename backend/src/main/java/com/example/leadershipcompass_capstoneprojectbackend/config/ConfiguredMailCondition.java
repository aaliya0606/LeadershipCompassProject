package com.example.leadershipcompass_capstoneprojectbackend.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * True only when plan email is switched on and the selected M365 provider has credentials.
 */
public class ConfiguredMailCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        if (!env.getProperty("app.mail.enabled", Boolean.class, false)) {
            return false;
        }
        String provider = env.getProperty("app.mail.provider", "smtp");
        if ("graph".equalsIgnoreCase(provider)) {
            return hasText(env, "app.mail.graph.tenant-id")
                    && hasText(env, "app.mail.graph.client-id")
                    && hasText(env, "app.mail.graph.client-secret")
                    && hasText(env, "app.mail.graph.sender");
        }
        return hasText(env, "spring.mail.username") && hasText(env, "spring.mail.password");
    }

    private static boolean hasText(Environment env, String key) {
        String value = env.getProperty(key);
        return value != null && !value.isBlank();
    }
}
