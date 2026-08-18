package com.java700.workforce.common.web;

import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Redis-backed counters so limits hold across horizontally scaled instances (local profile). */
@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
class RedisRateLimitStore implements RateLimitStore {

    private final StringRedisTemplate redis;

    RedisRateLimitStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean allow(String key, int limit) {
        String k = "ratelimit:" + key + ":" + (System.currentTimeMillis() / 60_000);
        Long count = redis.opsForValue().increment(k);
        if (count != null && count == 1) {
            redis.expire(k, Duration.ofMinutes(2));
        }
        return count == null || count <= limit;
    }
}
