package com.java700.workforce.common.cache;

import com.java700.workforce.policy.PolicyService;
import java.time.Duration;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class CacheConfig {

    public static final String ACTIVE_RULES_CACHE = PolicyService.CACHE_ACTIVE_RULES;

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
    CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .prefixCacheNameWith("workforce:");
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cfg)
                .initialCacheNames(Set.of(ACTIVE_RULES_CACHE))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
    CacheManager inMemoryCacheManager() {
        ConcurrentMapCacheManager mgr = new ConcurrentMapCacheManager(ACTIVE_RULES_CACHE);
        return mgr;
    }

    @Bean
    @ConditionalOnProperty(name = "app.cache.enabled", havingValue = "false")
    CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }
}
