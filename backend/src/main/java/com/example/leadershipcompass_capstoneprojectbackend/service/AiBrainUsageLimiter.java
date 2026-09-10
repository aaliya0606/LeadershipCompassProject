package com.example.leadershipcompass_capstoneprojectbackend.service;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * In-memory limits for AI-Brain chat and plan generation.
 * <p>
 * Caps how often each user can call expensive AI endpoints and how many
 * AI-Brain requests can run at once, so a single client cannot flood the
 * hosted model.
 */
@Service
public class AiBrainUsageLimiter {

    public enum Action {
        CHAT,
        PLAN
    }

    private final Clock clock;
    private final int chatMaxRequests;
    private final Duration chatWindow;
    private final int planMaxRequests;
    private final Duration planWindow;
    private final int userMaxConcurrent;
    private final int globalMaxConcurrent;

    private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> userInFlight = new ConcurrentHashMap<>();
    private final AtomicInteger globalInFlight = new AtomicInteger();

    @Autowired
    public AiBrainUsageLimiter(
            @Value("${app.ai-brain.limits.chat-max-requests:12}") int chatMaxRequests,
            @Value("${app.ai-brain.limits.chat-window-seconds:600}") long chatWindowSeconds,
            @Value("${app.ai-brain.limits.plan-max-requests:3}") int planMaxRequests,
            @Value("${app.ai-brain.limits.plan-window-seconds:3600}") long planWindowSeconds,
            @Value("${app.ai-brain.limits.user-max-concurrent:1}") int userMaxConcurrent,
            @Value("${app.ai-brain.limits.global-max-concurrent:6}") int globalMaxConcurrent) {
        this(
                Clock.systemUTC(),
                chatMaxRequests,
                Duration.ofSeconds(chatWindowSeconds),
                planMaxRequests,
                Duration.ofSeconds(planWindowSeconds),
                userMaxConcurrent,
                globalMaxConcurrent);
    }

    AiBrainUsageLimiter(
            Clock clock,
            int chatMaxRequests,
            Duration chatWindow,
            int planMaxRequests,
            Duration planWindow,
            int userMaxConcurrent,
            int globalMaxConcurrent) {
        this.clock = clock;
        this.chatMaxRequests = Math.max(1, chatMaxRequests);
        this.chatWindow = chatWindow;
        this.planMaxRequests = Math.max(1, planMaxRequests);
        this.planWindow = planWindow;
        this.userMaxConcurrent = Math.max(1, userMaxConcurrent);
        this.globalMaxConcurrent = Math.max(1, globalMaxConcurrent);
    }

    /**
     * Reserves a slot for one AI-Brain call. Callers must close the permit
     * in a {@code finally} block or try-with-resources so in-flight counts
     * are released.
     *
     * @param action chat or plan generation
     * @param userId authenticated user email
     * @return permit that releases in-flight slots when closed
     */
    public Permit acquire(Action action, String userId) {
        String userKey = userId == null || userId.isBlank() ? "anonymous" : userId.trim().toLowerCase();
        AtomicInteger userCount = userInFlight.computeIfAbsent(userKey, ignored -> new AtomicInteger());
        if (userCount.incrementAndGet() > userMaxConcurrent) {
            userCount.decrementAndGet();
            throw tooMany("Please wait for your current AI request to finish before starting another.");
        }

        if (globalInFlight.incrementAndGet() > globalMaxConcurrent) {
            globalInFlight.decrementAndGet();
            userCount.decrementAndGet();
            throw tooMany("The assistant is busy. Please try again in a moment.");
        }

        try {
            recordWindowHit(action, userKey);
        } catch (RuntimeException ex) {
            globalInFlight.decrementAndGet();
            userCount.decrementAndGet();
            throw ex;
        }

        return new Permit(userCount);
    }

    private void recordWindowHit(Action action, String userKey) {
        int max = action == Action.CHAT ? chatMaxRequests : planMaxRequests;
        Duration window = action == Action.CHAT ? chatWindow : planWindow;
        String windowKey = action.name() + ":" + userKey;
        long now = clock.millis();
        long cutoff = now - window.toMillis();

        Deque<Long> hits = windows.computeIfAbsent(windowKey, ignored -> new ArrayDeque<>());
        synchronized (hits) {
            while (!hits.isEmpty() && hits.peekFirst() <= cutoff) {
                hits.removeFirst();
            }
            if (hits.size() >= max) {
                String message = action == Action.CHAT
                        ? "Too many chat messages. Please wait a few minutes before asking again."
                        : "Too many plan generations. Please wait before generating another plan.";
                throw tooMany(message);
            }
            hits.addLast(now);
        }
    }

    private ResponseStatusException tooMany(String message) {
        return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    /**
     * Releases per-user and global in-flight slots.
     */
    public final class Permit implements AutoCloseable {
        private final AtomicInteger userCount;
        private boolean closed;

        private Permit(AtomicInteger userCount) {
            this.userCount = userCount;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            globalInFlight.decrementAndGet();
            userCount.decrementAndGet();
        }
    }
}
