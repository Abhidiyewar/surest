package com.surest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class CaffeineCacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        log.info("Initializing Caffeine cache configuration with 12-hour expiry and max size of 1000");
        return Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .maximumSize(1000)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("members");
        cacheManager.setCaffeine(caffeine);
        log.info("CaffeineCacheManager initialized successfully");
        return cacheManager;
    }
}
