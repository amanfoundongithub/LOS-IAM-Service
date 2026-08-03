package com.loan_org.identity_and_access_management.admin.model.account_lock;

import jakarta.validation.constraints.NotBlank;

/**
 * 
 * Defines the request body for locking a user's account by the admin on ground of 
 * suspicious or some malicious works.
 * 
 * @param reason The reason for locking the user's account
 * 
 */
public record UserAccountLockRequest(

        @NotBlank(message = "Please provide the reason for locking the account.")
        String reason,

        @NotBlank(message = "Please provide the userId for locking the account.")
        String userId

) {}