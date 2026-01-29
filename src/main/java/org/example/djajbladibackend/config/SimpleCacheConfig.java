package org.example.djajbladibackend.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * In-memory cache when Redis is not used (profile "noredis").
 * Use on Koyeb etc. when REDIS_URL / Redis is not configured.
 */
@Configuration
@Profile("noredis")
public class SimpleCacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        List<Cache> caches = Stream.of(
                        RedisCacheConfig.CACHE_USERS,
                        RedisCacheConfig.CACHE_EMAIL_EXISTS
                )
                .map(name -> new CacheHitTrackingCache(new ConcurrentMapCache(name)))
                .collect(Collectors.toList());

        org.springframework.cache.support.SimpleCacheManager manager = new org.springframework.cache.support.SimpleCacheManager();
        manager.setCaches(caches);
        manager.initializeCaches();
        return manager;
    }

    @Bean
    public OncePerRequestFilter cacheHitTrackingFilter() {
        return new CacheHitTrackingFilter();
    }
}
