package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.EmailSubscriptionDto;
import com.example.leadershipcompass_capstoneprojectbackend.dto.UpdateEmailSubscriptionRequest;
import com.example.leadershipcompass_capstoneprojectbackend.service.EmailSubscriptionService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unsubscribe link from plan emails, plus the signed-in user's preference.
 */
@RestController
public class EmailSubscriptionController {

    private final EmailSubscriptionService emailSubscriptionService;

    public EmailSubscriptionController(EmailSubscriptionService emailSubscriptionService) {
        this.emailSubscriptionService = emailSubscriptionService;
    }

    /**
     * Opts the recipient out when they open the link in a plan email.
     *
     * @param token unsubscribe token
     * @return a short confirmation page
     */
    @GetMapping(value = "/api/email/unsubscribe", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> unsubscribeFromLink(@RequestParam String token) {
        emailSubscriptionService.unsubscribe(token);
        return ResponseEntity.ok("""
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="utf-8"><title>Unsubscribed</title></head>
                <body>
                <p>You have been unsubscribed from Leadership Compass plan emails.</p>
                </body>
                </html>
                """);
    }

    /**
     * One-click unsubscribe (List-Unsubscribe-Post). The token is on the URL.
     *
     * @param token unsubscribe token
     * @return empty 200
     */
    @PostMapping("/api/email/unsubscribe")
    public ResponseEntity<Void> unsubscribeOneClick(@RequestParam String token) {
        emailSubscriptionService.unsubscribe(token);
        return ResponseEntity.ok().build();
    }

    /**
     * Reads whether the signed-in user still receives plan emails.
     *
     * @param principal current user
     * @return opt-out flag
     */
    @GetMapping("/api/email/subscription")
    public EmailSubscriptionDto current(Principal principal) {
        return new EmailSubscriptionDto(emailSubscriptionService.isOptedOut(principal.getName()));
    }

    /**
     * Opts the signed-in user in or out of plan emails.
     *
     * @param principal current user
     * @param request   desired preference
     * @return saved preference
     */
    @PutMapping("/api/email/subscription")
    public EmailSubscriptionDto update(
            Principal principal,
            @Valid @RequestBody UpdateEmailSubscriptionRequest request) {
        boolean optedOut = emailSubscriptionService.setOptedOut(principal.getName(), request.getOptedOut());
        return new EmailSubscriptionDto(optedOut);
    }
}
