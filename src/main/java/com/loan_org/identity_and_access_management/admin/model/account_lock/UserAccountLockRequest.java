package com.loan_org.identity_and_access_management.admin.model.account_lock;

import java.time.Instant;

import com.loan_org.identity_and_access_management.admin.shared.LockType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
        String userId,

        @NotNull(message = "Provide a lock type to determine if lock is temporary or permanent.")
        LockType lockType,

        Instant lockedUntil, 

        boolean notifyUser
) {
        
}
