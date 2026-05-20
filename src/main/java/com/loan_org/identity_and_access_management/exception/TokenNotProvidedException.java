package com.loan_org.identity_and_access_management.exception;

public class TokenNotProvidedException extends RuntimeException {
    public TokenNotProvidedException(String message) {
        super(message);
    }
}
