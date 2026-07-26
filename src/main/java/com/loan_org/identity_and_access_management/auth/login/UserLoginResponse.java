package com.loan_org.identity_and_access_management.auth.login;

import com.loan_org.identity_and_access_management.userEntity.dto.UserResponseDto;

public record UserLoginResponse(
    String accessToken,
    String refreshToken,
    UserResponseDto user
) {}