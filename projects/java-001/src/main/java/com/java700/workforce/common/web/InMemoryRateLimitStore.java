package com.java700.workforce.common.web;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Fixed-window counters held in memory (dev/test; not suitable for multi-node). */
@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
class InMemoryRateLimitStore implements RateLimitStore {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public boolean allow(String key, int limit) {
        long now = System.currentTimeMillis();
        long bucket = now / 60_000;
        Window w = windows.compute(key, (k, cur) ->
                cur == null || cur.bucket != bucket ? new Window(bucket, 0) : cur);
        return ++w.count <= limit;
    }

    private static final class Window {
        private final long bucket;
        private int count;

        private Window(long bucket, int count) {
            this.bucket = bucket;
            this.count = count;
        }
    }
}
