package com.loan_org.identity_and_access_management.rateLimiter;

import io.github.bucket4j.Bucket;

public interface RateLimiterService {
    Bucket resolveBucket(String apiKey);
}
