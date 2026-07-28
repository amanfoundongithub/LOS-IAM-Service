package com.loan_org.identity_and_access_management.auth.logout;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(

        @NotBlank(message = "Refresh token is required to revoke session")
        String refreshToken

) {}