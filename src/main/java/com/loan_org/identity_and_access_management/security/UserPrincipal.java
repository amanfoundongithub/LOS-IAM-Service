package com.loan_org.identity_and_access_management.security;

public record UserPrincipal(
        String email,
        String role
) {}