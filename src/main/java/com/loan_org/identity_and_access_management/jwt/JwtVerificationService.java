package com.loan_org.identity_and_access_management.jwt;

public interface JwtVerificationService {
    JwtUserClaims verify(String token);
}