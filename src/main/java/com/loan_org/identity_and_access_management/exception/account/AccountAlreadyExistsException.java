package com.loan_org.identity_and_access_management.exception.account;

public class AccountAlreadyExistsException extends RuntimeException {

    private static final String ACCOUNT_ALREADY_EXISTS_TEMPLATE = 
            """
            The user account with %s: %s already exists.
            """;

    public AccountAlreadyExistsException(String message) {
        super(message);
    }

    public AccountAlreadyExistsException(String key, String value) {
        super(String.format(ACCOUNT_ALREADY_EXISTS_TEMPLATE, key, value));
    }

    public AccountAlreadyExistsException(RuntimeException ex) {
        super(ex);
    }
    
}
