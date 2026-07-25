package com.loan_org.identity_and_access_management.jwt;

import java.util.Map;

public record JwtUserClaims(
    String email,
    Map<String, Object> attributes
) {}