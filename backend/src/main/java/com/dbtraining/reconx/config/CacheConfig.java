package com.dbtraining.reconx.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================================
 * TICKET-ADV081 — Enable Spring Cache
 *
 * WHAT:
 * Enables Spring's annotation-driven caching support.
 *
 * WHY:
 * Without @EnableCaching, @Cacheable annotations are ignored.
 *
 * ============================================================================
 */
@Configuration
@EnableCaching
public class CacheConfig {
}