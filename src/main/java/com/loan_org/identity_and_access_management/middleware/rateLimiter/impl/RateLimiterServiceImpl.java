package com.loan_org.identity_and_access_management.middleware.rateLimiter.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.loan_org.identity_and_access_management.middleware.rateLimiter.RateLimiterService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    private final int bucketCapacity;
    private final int refillTokens;
    private final Duration refillDuration;

    // Build a lazy caffeine cache
    private final Cache<String, Bucket> cache;

    // Constructor for cache
    public RateLimiterServiceImpl(
            @Value("${filter.rateLimiter.bucket.capacity}") int bucketCapacity,
            @Value("${filter.rateLimiter.bucket.refill}") int refillTokens,
            @Value("${filter.rateLimiter.bucket.intervalInMinutes}") int intervalMinutes,
            @Value("${filter.rateLimiter.bucket.cache.maxSize}") int maxCacheSize,
            @Value("${filter.rateLimiter.bucket.cache.expiresAfterHour}") int expiresAfterHours) {
                
                if (bucketCapacity <= 0) {
                    throw new IllegalArgumentException("Bucket capacity must be positive");
                }
                if (refillTokens <= 0) {
                    throw new IllegalArgumentException("Refill tokens must be positive");
                }
                if (intervalMinutes <= 0) {
                    throw new IllegalArgumentException("Refill interval must be positive");
                }

                this.bucketCapacity = bucketCapacity;
                this.refillTokens = refillTokens;
                this.refillDuration = Duration.ofMinutes(intervalMinutes);
                this.cache = Caffeine.newBuilder()
                    .maximumSize(maxCacheSize)
                    .expireAfterAccess(Duration.ofHours(expiresAfterHours))
                    .build();
    }

    @Override
    public Bucket resolveBucket(String identifier) {
        return cache.get(identifier, this::createBucket);
    }

    private Bucket createBucket(String ignored) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(bucketCapacity)
                .refillGreedy(refillTokens, refillDuration)
                .build();
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}
