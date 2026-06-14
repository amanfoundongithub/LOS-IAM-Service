package com.loan_org.identity_and_access_management.domain.auth.factory;

import com.loan_org.identity_and_access_management.domain.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;

public interface UserRegistrationFactory {
    UserDocument createPendingUser(UserRegistrationDto registrationDetails);
}
