package com.loan_org.identity_and_access_management.service;

import com.loan_org.identity_and_access_management.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.entity.UserDocument;

/**
 * Authentication service
 */
public interface AuthService {

    UserDocument    register(UserRegistrationDto registrationData);
    UserResponseDto loginWithEmail(String email, String password);
    UserResponseDto loginWithUsername(String username, String password);
}
