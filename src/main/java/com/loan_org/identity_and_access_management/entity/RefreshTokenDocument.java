package com.loan_org.identity_and_access_management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * MongoDB document representing a time-bound refresh token
 * associated with a user session.
 *
 * <p>Used to issue new access tokens without requiring
 * re-authentication while maintaining session continuity.</p>
 *
 * @author Aman Raj
 */
@Document(collection = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenDocument {

    @Id
    private String id;

    @JsonIgnore
    @Indexed(unique = true)
    @NotBlank
    private String token;

    @Indexed
    @NotBlank
    @Email
    private String userEmail;

    @Indexed(name = "refresh_token_expiry_idx", expireAfterSeconds = 0)
    private Instant expiresAt;

    public RefreshTokenDocument(String token, String userEmail, int expiryDays) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiresAt = Instant.now().plus(expiryDays, ChronoUnit.DAYS);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }
}