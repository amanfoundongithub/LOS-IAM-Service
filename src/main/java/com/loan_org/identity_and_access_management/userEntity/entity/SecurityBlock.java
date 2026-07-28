package com.loan_org.identity_and_access_management.userEntity.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.Instant;

/**
 * Embedded security model containing authentication and
 * account protection data associated with a user identity.
 *
 * Stores credential-related information, multi-factor
 * authentication configuration, login protection state,
 * and security lifecycle metadata.
 *
 * @author Aman Raj
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityBlock {

    @JsonIgnore
    @ToString.Exclude
    private String passwordHash;

    @JsonIgnore
    @ToString.Exclude
    private String mfaSecretEncrypted;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private boolean mfaEnabled = false;

    @Builder.Default
    private int failedLoginAttempts = 0;

    /**
     * Account remains locked until this instant.
     */
    private Instant lockoutUntil;

    /**
     * Last successful password change.
     */
    private Instant passwordChangedAt;

    /**
     * Last password reset request completion.
     */
    private Instant lastPasswordResetAt;

    /**
     * Last failed login attempt.
     */
    private Instant lastFailedLoginAt;

    /**
     * Last successful MFA verification.
     */
    private Instant lastMfaVerifiedAt;
}