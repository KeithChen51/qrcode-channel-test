package com.byd.qrcode.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RateLimitService {

    private static final int CLEANUP_THRESHOLD = 10_000;

    private final ConcurrentMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public boolean tryAcquire(String scope, String key, int maxRequests, long windowSeconds) {
        if (maxRequests <= 0 || windowSeconds <= 0) {
            return true;
        }

        long now = Instant.now().getEpochSecond();
        long windowEndsAt = now + windowSeconds;
        String counterKey = buildCounterKey(scope, key);
        AtomicBoolean allowed = new AtomicBoolean(false);

        counters.compute(counterKey, (_ignored, existing) -> {
            WindowCounter counter = existing;
            if (counter == null || counter.expiresAt <= now) {
                counter = new WindowCounter(0, windowEndsAt);
            }
            if (counter.count >= maxRequests) {
                allowed.set(false);
                return counter;
            }
            counter.count++;
            allowed.set(true);
            return counter;
        });

        if (counters.size() > CLEANUP_THRESHOLD) {
            cleanup(now);
        }
        return allowed.get();
    }

    public void reset(String scope, String key) {
        counters.remove(buildCounterKey(scope, key));
    }

    private String buildCounterKey(String scope, String key) {
        String normalizedScope = StringUtils.hasText(scope) ? scope.trim() : "default";
        String normalizedKey = StringUtils.hasText(key) ? key.trim() : "unknown";
        return normalizedScope + "|" + normalizedKey;
    }

    private void cleanup(long now) {
        counters.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
    }

    private static class WindowCounter {
        private int count;
        private final long expiresAt;

        private WindowCounter(int count, long expiresAt) {
            this.count = count;
            this.expiresAt = expiresAt;
        }
    }
}
