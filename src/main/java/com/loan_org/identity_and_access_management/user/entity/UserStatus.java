package com.loan_org.identity_and_access_management.user.entity;

/**
 * Represents the lifecycle and operational state
 * of a user account within the IAM system.
 *
 * @author Aman Raj
 */
public enum UserStatus {
    /**
     * Account created but email verification is pending.
     */
    PENDING_VERIFICATION,

    /**
     * Fully active account with normal access permissions.
     */
    ACTIVE,

    /**
     * Account temporarily restricted due to security
     * or administrative action.
     */
    LOCKED,

    /**
     * Account archived and no longer operational.
     */
    ARCHIVED
}
