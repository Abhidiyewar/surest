package com.surest.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import static org.junit.jupiter.api.Assertions.*;

class CaffeineCacheConfigTest {

    @Test
    @DisplayName("Caffeine and CacheManager are Configured and Usable")
    void caffeineCacheTest() {

        CaffeineCacheConfig config = new CaffeineCacheConfig();
        Caffeine<Object, Object> caffeine = config.caffeineConfig();
        CacheManager cacheManager = config.cacheManager(caffeine);


        assertNotNull(caffeine);
        assertNotNull(cacheManager);

        String builderStr = caffeine.toString();
        assertTrue(builderStr.contains("expireAfterWrite"));
        assertTrue(builderStr.contains("maximumSize=1000"));

        Cache<Object, Object> nativeCache = caffeine.build();
        long beforeRequests = nativeCache.stats().requestCount();
        nativeCache.put("email", "rahul@gmail.com");
        assertEquals("rahul@gmail.com", nativeCache.getIfPresent("email"));
        nativeCache.getIfPresent("email");
        assertTrue(nativeCache.stats().requestCount() >= beforeRequests + 1);

        org.springframework.cache.Cache springCache = cacheManager.getCache("members");
        assertNotNull(springCache);
        assertInstanceOf(CaffeineCache.class, springCache);
        springCache.put("c28ae94f-41a4-4321-a0b7-5b6e7103a90b", "Rahul");
        assertEquals("Rahul", springCache.get("c28ae94f-41a4-4321-a0b7-5b6e7103a90b", String.class));
    }
}
