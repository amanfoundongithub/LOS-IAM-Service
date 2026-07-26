package com.loan_org.identity_and_access_management.auth.login.service;

import com.loan_org.identity_and_access_management.userEntity.dto.UserResponseDto;

public interface JwtService {
    public String generateToken(UserResponseDto response);
    public String createRefreshToken();
    public String createPasswordResetToken();
    public String createAccountActivationToken();
}
