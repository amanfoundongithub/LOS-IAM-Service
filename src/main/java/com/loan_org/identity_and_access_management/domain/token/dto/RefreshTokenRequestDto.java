package com.loan_org.identity_and_access_management.domain.token.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDto {

    @NotBlank(message = "Refresh token value is required")
    private String refreshToken;

}