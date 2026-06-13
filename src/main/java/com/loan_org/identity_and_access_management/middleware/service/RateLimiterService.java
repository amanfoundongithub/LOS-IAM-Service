package com.loan_org.identity_and_access_management.middleware.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    @Value("${app.bucket.capacity}")
    private int bucketCapacity;

    @Value("${app.bucket.refill}")
    private int refillValue;

    @Value("${app.bucket.intervalInMinutes}")
    private int intervalInMinutes;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String apiKey) {
        return cache.computeIfAbsent(apiKey, this::createBucket);
    }

    private Bucket createBucket(String apiKey) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(bucketCapacity)
                .refillIntervally(refillValue, Duration.ofMinutes(intervalInMinutes))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
