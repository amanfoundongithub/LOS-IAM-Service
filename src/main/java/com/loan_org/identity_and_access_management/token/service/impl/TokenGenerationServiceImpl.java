package com.loan_org.identity_and_access_management.token.service.impl;

import java.util.UUID;

import com.loan_org.identity_and_access_management.token.service.TokenGenerationService;

public class TokenGenerationServiceImpl implements TokenGenerationService{

    @Override
    public String createRefreshToken() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }

    @Override
    public String createPasswordResetToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String createAccountActivationToken() {
        return UUID.randomUUID().toString();
    }
}
