package com.loan_org.identity_and_access_management.auth.dto;

public record PasswordChangeRequestDto(
        String oldPassword,
        String newPassword
) {}
