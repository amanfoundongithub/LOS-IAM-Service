package com.loan_org.identity_and_access_management.middleware.service;

public interface JwtVerificationService {
    String  extractEmail(String token);
    String  extractRole(String token);
    boolean isTokenValid(String token);
}
