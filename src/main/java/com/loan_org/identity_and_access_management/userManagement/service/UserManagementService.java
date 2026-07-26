package com.loan_org.identity_and_access_management.userManagement.service;

import com.loan_org.identity_and_access_management.auth.passwordReset.PasswordChangeRequestDto;
import com.loan_org.identity_and_access_management.userEntity.dto.UserResponseDto;

public interface UserManagementService {
    void            updatePassword(String email, PasswordChangeRequestDto changeRequest);
    UserResponseDto fetchUserByEmail(String email);
}
