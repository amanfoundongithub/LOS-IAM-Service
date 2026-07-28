package com.loan_org.identity_and_access_management.exception.account;

public class AccountNotFoundException extends RuntimeException {

    private static final String ACCOUNT_NOT_FOUND_TEMPLATE = 
            """
            The user account with %s: %s is not found in database.
            """;

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String key, String value) {
        super(String.format(ACCOUNT_NOT_FOUND_TEMPLATE, key, value));
    }

    public AccountNotFoundException(RuntimeException ex) {
        super(ex);
    }
    
}
