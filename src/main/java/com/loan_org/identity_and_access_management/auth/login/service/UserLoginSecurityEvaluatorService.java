package com.loan_org.identity_and_access_management.auth.login.service;

import com.loan_org.identity_and_access_management.user.entity.UserDocument;

public interface UserLoginSecurityEvaluatorService {
    void verifyAccountPolicies(UserDocument document);
    void verifyCredentials(UserDocument document, String plainPassword);
}
