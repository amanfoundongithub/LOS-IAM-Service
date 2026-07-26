package com.loan_org.identity_and_access_management.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a user login request.
 *
 * <p>
 * Either {@code email} or {@code username} must be supplied,
 * together with the user's password.
 * </p>
 *
 * @author Aman Raj
 * @since 1.0.0
 */
public record UserLoginDto(

        /**
         * Registered email address of the account.
         */
        @Email(message = "Invalid email format.")
        String email,

        /**
         * Unique username of the account.
         */
        String username,

        /**
         * Plain-text password.
         */
        @NotBlank(message = "Password is required.")
        String password

) {

    /**
     * Ensures at least one user identifier is provided.
     *
     * @return {@code true} if either email or username is present.
     */
    @AssertTrue(message = "Either username or email must be provided.")
    public boolean hasValidIdentifier() {

        boolean emailPresent =
                email != null && !email.isBlank();

        boolean usernamePresent =
                username != null && !username.isBlank();

        return emailPresent || usernamePresent;
    }
}