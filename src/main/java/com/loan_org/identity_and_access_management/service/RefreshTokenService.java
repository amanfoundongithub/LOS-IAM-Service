package com.loan_org.identity_and_access_management.service;

import com.loan_org.identity_and_access_management.dto.RefreshTokenRequestDto;

public interface RefreshTokenService {
    String refreshToken(RefreshTokenRequestDto request);
}
