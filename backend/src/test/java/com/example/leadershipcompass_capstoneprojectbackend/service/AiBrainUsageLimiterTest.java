package com.example.leadershipcompass_capstoneprojectbackend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.leadershipcompass_capstoneprojectbackend.service.AiBrainUsageLimiter.Action;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for per-user and global AI-Brain usage limits.
 */
class AiBrainUsageLimiterTest {

    @Test
    void rejectsChatAfterWindowIsExhausted() {
        MutableClock clock = new MutableClock();
        AiBrainUsageLimiter limiter = new AiBrainUsageLimiter(
                clock, 2, Duration.ofMinutes(10), 3, Duration.ofHours(1), 2, 6);

        try (AiBrainUsageLimiter.Permit first = limiter.acquire(Action.CHAT, "sam@test.com")) {
            assertDoesNotThrow(() -> limiter.acquire(Action.CHAT, "sam@test.com").close());
            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> limiter.acquire(Action.CHAT, "sam@test.com"));
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        }
    }

    @Test
    void allowsChatAgainAfterWindowElapses() {
        MutableClock clock = new MutableClock();
        AiBrainUsageLimiter limiter = new AiBrainUsageLimiter(
                clock, 1, Duration.ofMinutes(10), 3, Duration.ofHours(1), 1, 6);

        limiter.acquire(Action.CHAT, "sam@test.com").close();
        clock.advance(Duration.ofMinutes(10));
        assertDoesNotThrow(() -> limiter.acquire(Action.CHAT, "sam@test.com").close());
    }

    @Test
    void limitsPlanGenerationSeparatelyFromChat() {
        MutableClock clock = new MutableClock();
        AiBrainUsageLimiter limiter = new AiBrainUsageLimiter(
                clock, 12, Duration.ofMinutes(10), 1, Duration.ofHours(1), 2, 6);

        limiter.acquire(Action.PLAN, "sam@test.com").close();
        assertDoesNotThrow(() -> limiter.acquire(Action.CHAT, "sam@test.com").close());
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> limiter.acquire(Action.PLAN, "sam@test.com"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
    }

    @Test
    void rejectsASecondConcurrentRequestForTheSameUser() {
        AiBrainUsageLimiter limiter = new AiBrainUsageLimiter(
                Clock.systemUTC(), 12, Duration.ofMinutes(10), 3, Duration.ofHours(1), 1, 6);

        try (AiBrainUsageLimiter.Permit ignored = limiter.acquire(Action.CHAT, "sam@test.com")) {
            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> limiter.acquire(Action.PLAN, "sam@test.com"));
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        }
    }

    @Test
    void rejectsWhenGlobalConcurrentCapacityIsFull() {
        AiBrainUsageLimiter limiter = new AiBrainUsageLimiter(
                Clock.systemUTC(), 12, Duration.ofMinutes(10), 3, Duration.ofHours(1), 1, 1);

        try (AiBrainUsageLimiter.Permit ignored = limiter.acquire(Action.CHAT, "sam@test.com")) {
            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> limiter.acquire(Action.CHAT, "alex@test.com"));
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        }
    }

    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant = new AtomicReference<>(Instant.parse("2026-09-07T02:00:00Z"));

        void advance(Duration duration) {
            instant.updateAndGet(current -> current.plus(duration));
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }
}
