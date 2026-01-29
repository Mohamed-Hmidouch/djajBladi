package org.example.djajbladibackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.filter.OncePerRequestFilter;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration.
 * First request hits the backend; subsequent requests with same key are served from Redis.
 */
@Configuration
public class RedisCacheConfig {

    public static final String CACHE_USERS = "users";
    public static final String CACHE_EMAIL_EXISTS = "emailExists";

    @Value("${spring.cache.redis.time-to-live:600000}")
    private long defaultTtlMs;

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(defaultTtlMs))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(CACHE_USERS, defaultConfig);
        cacheConfigurations.put(CACHE_EMAIL_EXISTS, defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /** CacheManager that wraps Redis caches to track HIT/MISS for X-Cache-Status header. */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisCacheManager redisCacheManager) {
        return new CacheManager() {
            @Override
            public Cache getCache(String name) {
                Cache redisCache = redisCacheManager.getCache(name);
                return redisCache != null ? new CacheHitTrackingCache(redisCache) : null;
            }

            @Override
            public java.util.Collection<String> getCacheNames() {
                return redisCacheManager.getCacheNames();
            }
        };
    }

    @Bean
    public OncePerRequestFilter cacheHitTrackingFilter() {
        return new CacheHitTrackingFilter();
    }
}
