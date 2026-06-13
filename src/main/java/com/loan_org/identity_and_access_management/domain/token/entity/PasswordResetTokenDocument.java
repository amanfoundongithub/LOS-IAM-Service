package com.loan_org.identity_and_access_management.domain.token.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * MongoDB document representing a time-bound password reset token
 * associated with a user identity.
 *
 * <p>Used during credential recovery workflows to authorize
 * secure password reset operations.</p>
 *
 * @author Aman Raj
 */
@Document(collection = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetTokenDocument {

    @Id
    @Setter(AccessLevel.NONE) // Protect database-assigned ID immutability
    private String id;

    @Indexed(unique = true)
    @NotBlank
    private String token;

    @Indexed
    @NotBlank
    @Email
    private String userEmail;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    /**
     * Runtime validation utility ensuring token freshness.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }
}