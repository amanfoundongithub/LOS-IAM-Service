package com.loan_org.identity_and_access_management.token.service;

public interface TokenGenerationService {
    public String createRefreshToken();
    public String createPasswordResetToken();
    public String createAccountActivationToken();
}
