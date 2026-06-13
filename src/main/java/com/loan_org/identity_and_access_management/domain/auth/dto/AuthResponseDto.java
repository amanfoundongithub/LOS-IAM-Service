package com.loan_org.identity_and_access_management.domain.auth.dto;

import com.loan_org.identity_and_access_management.domain.user.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserResponseDto user;

    public AuthResponseDto(String token, String refreshToken, UserResponseDto user) {
        this.accessToken = token;
        this.user = user;
        this.refreshToken = refreshToken;
    }
}