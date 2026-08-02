package com.loan_org.identity_and_access_management.token.refresh;

import com.loan_org.identity_and_access_management.auth.logout.LogoutRequest;
import com.loan_org.identity_and_access_management.auth.refreshToken.RefreshTokenRequest;

public interface RefreshTokenService {
    /**
     * Generates a new refresh token for a user session
     *
     * @param request The request for generation
     * @return The new generated token
     */
    String generateRefreshToken(RefreshTokenRequest request);

    /**
     * Generates a new refresh token for a user session
     *
     * @param email The user email
     * @return The new generated token
     */
    String generateRefreshToken(String email);

    /**
     * Revokes the existing refresh token, if any
     *
     * @param request The refresh token to be revoked
     */
    void   revokeRefreshToken(LogoutRequest request);

    String loginUsingRefreshToken(RefreshTokenRequest request);

    /**
     * Revokes all refresh tokens for a given email
     *
     * @param email The email to be revoked
     */
    void   revokeRefreshToken(String email);
}
