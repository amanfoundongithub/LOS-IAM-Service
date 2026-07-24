package com.loan_org.identity_and_access_management.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRevokeDto(

        @NotBlank(message = "Refresh token is required to revoke session")
        String refreshToken

) {}