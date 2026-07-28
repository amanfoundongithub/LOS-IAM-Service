package com.loan_org.identity_and_access_management.exception.security;

public class AccountCurrentlyLockedException extends RuntimeException {

    private static final String ACCOUNT_CURRENTLY_LOCKED_DUE_TO_SECURITY_TEMPLATE = 
            """
            The account with %s: %s is locked for %d %s.
            """;
    
    private static final String ACCOUNT_CURRENTLY_LOCKED_BY_ADMIN_TEMPLATE =
            """
            The account with %s: %s is locked by an admin.      
            """;

    public AccountCurrentlyLockedException(String message) {
        super(message);
    }

    public AccountCurrentlyLockedException(String key, String value) {
        super(String.format(ACCOUNT_CURRENTLY_LOCKED_BY_ADMIN_TEMPLATE, key, value));
    }

    public AccountCurrentlyLockedException(String key, String value, long timeLeft, String units) {
        super(String.format(ACCOUNT_CURRENTLY_LOCKED_DUE_TO_SECURITY_TEMPLATE, key, value, timeLeft, units));
    }

}
