package com.loan_org.identity_and_access_management.auth.service;

import com.loan_org.identity_and_access_management.user.entity.UserDocument;

public interface LoginSecurityEvaluatorService {
    void verifyAccountPolicies(UserDocument document);
    void verifyCredentials(UserDocument document, String plainPassword);
}
