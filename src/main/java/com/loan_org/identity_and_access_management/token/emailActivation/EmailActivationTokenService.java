package com.loan_org.identity_and_access_management.token.emailActivation;

public interface EmailActivationTokenService {
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
}
