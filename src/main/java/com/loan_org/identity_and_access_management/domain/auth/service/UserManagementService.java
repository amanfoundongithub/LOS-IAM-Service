package com.loan_org.identity_and_access_management.domain.auth.service;

import com.loan_org.identity_and_access_management.domain.auth.dto.PasswordChangeRequestDto;
import com.loan_org.identity_and_access_management.domain.user.dto.UserResponseDto;

public interface UserManagementService {
    void            updatePassword(String email, PasswordChangeRequestDto changeRequest);
    UserResponseDto fetchUserByEmail(String email);
}
