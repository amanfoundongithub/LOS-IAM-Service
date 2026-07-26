package com.loan_org.identity_and_access_management.auth.register;

import com.loan_org.identity_and_access_management.user.dto.UserResponseDto;

public record UserRegistrationResponse(
    String accessToken,
    String refreshToken,
    UserResponseDto user
) {}