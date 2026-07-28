package com.loan_org.identity_and_access_management.admin.model.account_unlock;

import jakarta.validation.constraints.NotBlank;

/**
 * 
 * Defines the request body for unlocking a user's account by the admin on ground of 
 * false suspicious made previously.
 * 
 * @param reason The reason for unlocking the account.
 * 
 */
public record UserAccountUnlockRequest(

        @NotBlank(message = "Please provide the reason for unlocking the account.")
        String reason,

        @NotBlank(message = "Please provide the userId for locking the account.")
        String userId

){}
