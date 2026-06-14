package com.loan_org.identity_and_access_management.domain.auth.service;

import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;

public interface RegistrationWorkflowCoordinator {
    void initiatePostRegistration(UserDocument user);
}
