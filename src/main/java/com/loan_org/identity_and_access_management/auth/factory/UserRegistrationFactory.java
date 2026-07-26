package com.loan_org.identity_and_access_management.auth.factory;

import com.loan_org.identity_and_access_management.auth.register.UserRegistrationRequest;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;

public interface UserRegistrationFactory {
    UserDocument createPendingUser(UserRegistrationRequest registrationDetails);
}
