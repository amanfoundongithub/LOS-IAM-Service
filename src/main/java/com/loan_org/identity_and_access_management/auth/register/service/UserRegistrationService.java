package com.loan_org.identity_and_access_management.auth.register.service;

import com.loan_org.identity_and_access_management.auth.register.UserRegistrationRequest;
import com.loan_org.identity_and_access_management.auth.register.UserRegistrationResponse;

public interface UserRegistrationService {
    UserRegistrationResponse register(UserRegistrationRequest request);
}
