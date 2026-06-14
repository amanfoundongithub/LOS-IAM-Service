package com.loan_org.identity_and_access_management.middleware.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.loan_org.identity_and_access_management.middleware.service.RateLimiterService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterServiceHelperimpl implements RateLimiterService {

    @Value("${filter.rateLimiter.bucket.capacity}")
    private int bucketCapacity;

    @Value("${filter.rateLimiter.bucket.refill}")
    private int refillValue;

    @Value("${filter.rateLimiter.bucket.intervalInMinutes}")
    private int intervalInMinutes;

    // Build a lazy caffeine cache
    private final Cache<String, Bucket> cache;

    // Constructor for cache
    public RateLimiterServiceHelperimpl(
            @Value("${filter.rateLimiter.bucket.cache.maxSize}") int maxCacheSize,
            @Value("${filter.rateLimiter.bucket.cache.expiresAfterHour}") int expiresAfterHour) {

        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(expiresAfterHour))
                .maximumSize(maxCacheSize)
                .build();
    }

    @Override
    public Bucket resolveBucket(String apiKey) {
        // adjusted as per cache needs
        return cache.get(apiKey, this::createBucket);
    }

    private Bucket createBucket(String apiKey) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(bucketCapacity)
                .refillGreedy(refillValue, Duration.ofMinutes(intervalInMinutes))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
