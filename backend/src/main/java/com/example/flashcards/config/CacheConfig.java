package com.example.flashcards.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer aiCacheTtl() {
        return builder -> builder
                .withCacheConfiguration("ai-hints",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(24)))
                .withCacheConfiguration("ai-sentences",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(72)));
    }
}
