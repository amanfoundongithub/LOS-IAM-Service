package com.loan_org.identity_and_access_management.middleware.service;

import io.github.bucket4j.Bucket;

public interface RateLimiterService {
    Bucket resolveBucket(String apiKey);
}
