package com.loan_org.identity_and_access_management.domain.auth.service;

import com.loan_org.identity_and_access_management.domain.auth.dto.UserLoginDto;
import com.loan_org.identity_and_access_management.domain.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.domain.user.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;

/**
 * Authentication service
 */
public interface AuthService {

    UserDocument    register(UserRegistrationDto registrationData);
    UserResponseDto login(UserLoginDto loginRequest);
}
