package com.loan_org.identity_and_access_management.auth.refreshToken;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used to obtain a new access token using a valid refresh token.
 *
 * @param refreshToken the refresh token previously issued during authentication
 *
 * @author Aman Raj
 * @since 1.0.0
 */
@Schema(description = "Refresh token request")
public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token is required.")
        @Schema(
                description = "Previously issued refresh token",
                example = "9c4b5f77-6b79-4d5b-a4e0-53e8f9d0b1aa-2d7d5d2b-5c3b-4a61-9d0d-0a9c9b8c5f1a"
        )
        String refreshToken

) {
}