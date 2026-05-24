package com.loan_org.identity_and_access_management.entity;

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
    private String  passwordHash;

    @JsonIgnore
    private String  mfaSecretEncrypted;

    private boolean emailVerified;
    private boolean mfaEnabled;
    private int     failedLoginAttempts;
    private Instant lockoutUntil;
    private Instant passwordChangedAt;
}
