package com.example.leadershipcompass_capstoneprojectbackend.config;

import com.example.leadershipcompass_capstoneprojectbackend.service.GraphPlanMailTransport;
import com.example.leadershipcompass_capstoneprojectbackend.service.PlanMailTransport;
import com.example.leadershipcompass_capstoneprojectbackend.service.SmtpPlanMailTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestClient;

/**
 * Registers the M365 transport only when mail is enabled and credentials are present.
 */
@Configuration
public class MailTransportConfig {

    private static final Logger log = LoggerFactory.getLogger(MailTransportConfig.class);

    @Bean
    @Conditional(ConfiguredMailCondition.class)
    public PlanMailTransport planMailTransport(Environment env, ObjectProvider<JavaMailSender> mailSender) {
        String provider = env.getProperty("app.mail.provider", "smtp");
        if ("graph".equalsIgnoreCase(provider)) {
            return new GraphPlanMailTransport(
                    RestClient.builder().build(),
                    env.getProperty("app.mail.graph.tenant-id"),
                    env.getProperty("app.mail.graph.client-id"),
                    env.getProperty("app.mail.graph.client-secret"),
                    env.getProperty("app.mail.graph.sender"));
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("SMTP mail is enabled but JavaMailSender is not available.");
        }
        return new SmtpPlanMailTransport(sender);
    }

    @Bean
    public ApplicationRunner mailConfigurationCheck(Environment env, ObjectProvider<PlanMailTransport> transport) {
        return args -> {
            boolean enabled = env.getProperty("app.mail.enabled", Boolean.class, false);
            if (!enabled) {
                log.info("Plan email is off until M365 SMTP or Graph credentials are configured.");
                return;
            }
            if (transport.getIfAvailable() == null) {
                log.warn(
                        "Plan email is enabled but {} credentials are incomplete, so sends will be skipped.",
                        env.getProperty("app.mail.provider", "smtp"));
            }
        };
    }
}
