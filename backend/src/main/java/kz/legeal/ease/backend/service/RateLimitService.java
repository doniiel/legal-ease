package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory sliding-window rate limiter.
 *
 * <p>Limits are enforced per (userId, action) pair. Counters are reset after the window elapses.
 * For distributed deployments this should be backed by Redis; for single-instance this is sufficient.
 */
@Service
public class RateLimitService {

    /** Default: 10 AI requests per minute per user. */
    @Value("${app.rate-limit.ai.requests-per-minute:10}")
    private int aiRequestsPerMinute;

    /** Default: 5 document completions per hour per user. */
    @Value("${app.rate-limit.complete.requests-per-hour:20}")
    private int completeRequestsPerHour;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public void checkAiLimit(Long userId) {
        checkLimit("ai:" + userId, aiRequestsPerMinute, 60_000L, "AI_RATE_LIMIT");
    }

    public void checkCompleteLimit(Long userId) {
        checkLimit("complete:" + userId, completeRequestsPerHour, 3_600_000L, "COMPLETE_RATE_LIMIT");
    }

    private void checkLimit(String key, int maxRequests, long windowMs, String errorCode) {
        final long now = Instant.now().toEpochMilli();
        final var window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.startMs > windowMs) {
                return new Window(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (window.count.get() > maxRequests) {
            throw new BusinessRuleException(
                    "Rate limit exceeded. Please wait before making more requests.",
                    errorCode,
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }
    }

    private record Window(long startMs, AtomicInteger count) {}
}
