package com.loan_org.identity_and_access_management.middleware;

public record UserPrincipal(
        String email,
        String role
) {}