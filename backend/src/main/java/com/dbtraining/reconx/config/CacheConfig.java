package com.dbtraining.reconx.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ============================================================================
 * TICKET-ADV082 — Cache eviction: TTL 5 min instruments, 1 min counterparties
 *
 * Creates two independent Caffeine caches:
 *
 * instruments:
 *      TTL: 5 minutes
 *      max size: 500
 *
 * counterparties:
 *      TTL: 1 minute
 *      max size: 200
 *
 * recordStats() enables Micrometer cache metrics.
 * ============================================================================
 */
@Configuration
public class CacheConfig {


    @Bean
    public CacheManager cacheManager() {

        CaffeineCache instruments =
                new CaffeineCache(
                        "instruments",
                        Caffeine.newBuilder()
                                .maximumSize(500)
                                .expireAfterWrite(5, TimeUnit.MINUTES)
                                .recordStats()
                                .build()
                );


        CaffeineCache counterparties =
                new CaffeineCache(
                        "counterparties",
                        Caffeine.newBuilder()
                                .maximumSize(200)
                                .expireAfterWrite(1, TimeUnit.MINUTES)
                                .recordStats()
                                .build()
                );


        SimpleCacheManager cacheManager = new SimpleCacheManager();

        cacheManager.setCaches(
                List.of(
                        instruments,
                        counterparties
                )
        );

        return cacheManager;
    }
}