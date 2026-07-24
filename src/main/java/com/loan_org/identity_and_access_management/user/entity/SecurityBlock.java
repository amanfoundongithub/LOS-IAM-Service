package com.loan_org.identity_and_access_management.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.Instant;

/**
 * Embedded security model containing authentication and
 * account protection data associated with a user identity.
 *
 * <p>Stores credential-related information, multi-factor
 * authentication configuration, login protection state,
 * and security lifecycle metadata.</p>
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
    private String  passwordHash;

    @JsonIgnore
    @ToString.Exclude
    private String  mfaSecretEncrypted;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private boolean mfaEnabled = false;

    @Builder.Default
    private int failedLoginAttempts = 0;

    private Instant lockoutUntil;
    private Instant passwordChangedAt;
}
