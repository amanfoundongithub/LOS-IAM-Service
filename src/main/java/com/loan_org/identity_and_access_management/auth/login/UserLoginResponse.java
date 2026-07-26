package com.loan_org.identity_and_access_management.auth.login;

import com.loan_org.identity_and_access_management.user.dto.UserResponseDto;

public record UserLoginResponse(
    String accessToken,
    String refreshToken,
    UserResponseDto user
) {}