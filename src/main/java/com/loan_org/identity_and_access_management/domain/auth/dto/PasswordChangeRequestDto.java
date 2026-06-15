package com.loan_org.identity_and_access_management.domain.auth.dto;

public record PasswordChangeRequestDto(
        String oldPassword,
        String newPassword
) {}
