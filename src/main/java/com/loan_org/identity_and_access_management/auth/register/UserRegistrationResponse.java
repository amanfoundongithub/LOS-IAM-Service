package com.loan_org.identity_and_access_management.auth.register;

import com.loan_org.identity_and_access_management.userEntity.dto.UserResponseDto;

import lombok.Builder;

@Builder
public record UserRegistrationResponse(
    UserResponseDto user
) {}