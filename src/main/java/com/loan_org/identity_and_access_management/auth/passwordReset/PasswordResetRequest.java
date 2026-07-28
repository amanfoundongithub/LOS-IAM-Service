package com.loan_org.identity_and_access_management.auth.passwordReset;

public record PasswordResetRequest(
        String oldPassword,
        String newPassword
) {}
