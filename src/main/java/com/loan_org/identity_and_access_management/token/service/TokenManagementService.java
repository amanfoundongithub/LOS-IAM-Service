package com.loan_org.identity_and_access_management.token.service;

import com.loan_org.identity_and_access_management.auth.dto.RefreshTokenRevokeDto;
import com.loan_org.identity_and_access_management.auth.refreshToken.RefreshTokenRequest;

/**
 * Utility service to consider token creation for
 * <ul>
 *     <li>JWT authentication</li>
 *     <li>Refresh token</li>
 *     <li>Account Activation Token</li>
 * </ul>
 *
 * Use this interface for dependency injection (DI) into appropriate controllers.
 *
 * @author Aman Raj
 * @since 20th May 2026
 */
public interface TokenManagementService {

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
    void   revokeRefreshToken(RefreshTokenRevokeDto request);

    /**
     * Revokes all refresh tokens for a given email
     *
     * @param email The email to be revoked
     */
    void   revokeRefreshToken(String email);

    /**
     * Generates a new activation token for user's email confirmation
     *
     * @param email The email of the user to be confirmed
     * @return The new generated token
     */
    String generateActivationToken(String email);

    /**
     * Verifies an activation token for user's email confirmation
     * @param activationToken The token
     *
     */
    void verifyActivationToken(String activationToken);

    /**
     * Generates a new token for password reset.
     *
     * @param email The email of the user
     *
     */
    void generatePasswordResetToken(String email);

    /**
     * Verifies a password reset token for user's password reset
     *
     * @param passwordResetToken The token for the password
     */
    void verifyPasswordResetToken(String passwordResetToken, String newPassword);

}
