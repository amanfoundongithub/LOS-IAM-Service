package com.loan_org.identity_and_access_management.exception.security;

public class InvalidPasswordException extends RuntimeException {

    private static final String INVALID_PASSWORD_TEMPLATE = 
            """
            The password for %s: %s is incorrect. Please try again.    
            """;
    
    private static final String INVALID_PASSWORD_WITH_ATTEMPTS_TEMPLATE = 
            """
            The password for %s: %s is incorrect. Attempts left: %d/%d     
            """;

    public InvalidPasswordException(String message) {
        super(message);
    }

    public InvalidPasswordException(String key, String value) {
        super(String.format(INVALID_PASSWORD_TEMPLATE, key, value));
    }

    public InvalidPasswordException(String key, String value, int attemptsLeft, int maxAttempts) {
        super(String.format(INVALID_PASSWORD_WITH_ATTEMPTS_TEMPLATE, key, value, attemptsLeft, maxAttempts));
    }
    
}
