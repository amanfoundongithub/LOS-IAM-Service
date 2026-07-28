package com.loan_org.identity_and_access_management.token.passwordReset;

public interface PasswordResetTokenService {
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
