package com.loan_org.identity_and_access_management.domain.auth.service;

import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;

public interface LoginSecurityEvaluatorService {
    void verifyAccountPolicies(UserDocument document);
    void verifyCredentials(UserDocument document, String plainPassword);
}
