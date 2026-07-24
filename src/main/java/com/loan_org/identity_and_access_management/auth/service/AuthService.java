package com.loan_org.identity_and_access_management.auth.service;

import com.loan_org.identity_and_access_management.auth.dto.UserLoginDto;
import com.loan_org.identity_and_access_management.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.user.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.user.entity.UserDocument;

/**
 * Authentication service
 */
public interface AuthService {

    UserDocument    register(UserRegistrationDto registrationData);
    UserResponseDto login(UserLoginDto loginRequest);

}
