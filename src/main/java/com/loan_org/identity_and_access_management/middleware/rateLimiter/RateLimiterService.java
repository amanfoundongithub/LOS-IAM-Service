package com.loan_org.identity_and_access_management.middleware.rateLimiter;

import io.github.bucket4j.Bucket;

public interface RateLimiterService {
    Bucket resolveBucket(String apiKey);
}
