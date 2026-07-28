package com.loan_org.identity_and_access_management.auth.register.service;

import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;

public interface RegistrationWorkflowCoordinator {
    void initiatePostRegistration(UserDocument user);
}
