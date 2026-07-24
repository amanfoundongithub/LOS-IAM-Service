package com.loan_org.identity_and_access_management.auth.service;

import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;

public interface RegistrationWorkflowCoordinator {
    void initiatePostRegistration(UserDocument user);
}
