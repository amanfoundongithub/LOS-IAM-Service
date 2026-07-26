package com.loan_org.identity_and_access_management.auth.passwordReset;

public record PasswordChangeRequestDto(
        String oldPassword,
        String newPassword
) {}
