package com.loan_org.identity_and_access_management.entity.user;

import com.loan_org.identity_and_access_management.entity.SecurityBlock;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SecurityBlockTest {

    @Test
    void testGettersSettersAndNoArgsConstructor() {
        // Arrange & Act (Testing No-Args Constructor & Setters)
        SecurityBlock securityBlock = new SecurityBlock();
        Instant now = Instant.now();

        securityBlock.setPasswordHash("hashed_pwd");
        securityBlock.setMfaSecretEncrypted("encrypted_mfa");
        securityBlock.setEmailVerified(true);
        securityBlock.setMfaEnabled(true);
        securityBlock.setFailedLoginAttempts(3);
        securityBlock.setLockoutUntil(now);
        securityBlock.setPasswordChangedAt(now);

        // Assert (Testing Getters)
        assertThat(securityBlock.getPasswordHash()).isEqualTo("hashed_pwd");
        assertThat(securityBlock.getMfaSecretEncrypted()).isEqualTo("encrypted_mfa");
        assertThat(securityBlock.isEmailVerified()).isTrue();
        assertThat(securityBlock.isMfaEnabled()).isTrue();
        assertThat(securityBlock.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(securityBlock.getLockoutUntil()).isEqualTo(now);
        assertThat(securityBlock.getPasswordChangedAt()).isEqualTo(now);
    }

    @Test
    void testAllArgsConstructorAndBuilder() {
        Instant now = Instant.now();

        // Act (Testing Builder and All-Args Constructor)
        SecurityBlock securityBlock = SecurityBlock.builder()
                .passwordHash("hashed_pwd")
                .mfaSecretEncrypted("encrypted_mfa")
                .emailVerified(true)
                .mfaEnabled(false)
                .failedLoginAttempts(0)
                .lockoutUntil(now)
                .passwordChangedAt(now)
                .build();

        // Assert
        assertThat(securityBlock).isNotNull();
        assertThat(securityBlock.getPasswordHash()).isEqualTo("hashed_pwd");
        assertThat(securityBlock.isMfaEnabled()).isFalse();
        assertThat(securityBlock.getFailedLoginAttempts()).isZero();
    }
}
