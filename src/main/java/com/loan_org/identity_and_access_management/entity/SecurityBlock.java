package com.loan_org.identity_and_access_management.entity;

import lombok.Data;
import java.time.Instant;

/**
 * Security block to keep the information related
 * to security aspects of the Identity.
 *
 * @author Aman Raj
 */
@Data
public class SecurityBlock {
    private String  passwordHash;
    private boolean emailVerified;
    private boolean mfaEnabled;
    private String  mfaSecretEncrypted;
    private int     failedLoginAttempts;
    private Instant lockoutUntil;
    private Instant passwordChangedAt;
}
