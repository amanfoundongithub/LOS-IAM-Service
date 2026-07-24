package com.loan_org.identity_and_access_management.auth.factory;

import com.loan_org.identity_and_access_management.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.user.entity.UserDocument;

public interface UserRegistrationFactory {
    UserDocument createPendingUser(UserRegistrationDto registrationDetails);
}
