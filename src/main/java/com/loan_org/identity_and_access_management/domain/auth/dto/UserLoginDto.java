package com.loan_org.identity_and_access_management.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing a multi-identifier user authentication request.
 * <p>
 * This design maintains dedicated fields for both email and username, enforcing a conditional
 * cross-field constraint requiring at least one of these identifiers to be populated.
 * </p>
 *
 * @author Aman Raj
 * @since 1.0.0
 */
@Getter
@Setter
public class UserLoginDto {

    /**
     * The registered email address of the account.
     * Highly optional if a valid {@code username} is supplied.
     */
    private String email;

    /**
     * The unique username of the account.
     * Highly optional if a valid {@code email} is supplied.
     */
    private String username;

    /**
     * The plain-text password credential associated with the account.
     */
    @NotBlank(message = "Password is required.")
    private String password;

    /**
     * Evaluates the cross-field constraint to ensure a valid identification state.
     * <p>
     * This helper method is intercepted during Java Bean Validation execution to verify that
     * the incoming request payload contains at least one non-blank identification claim.
     * </p>
     *
     * @return {@code true} if either the email or username is populated; {@code false} otherwise
     */
    @AssertTrue(message = "Either a username or email must be provided.")
    public boolean isValidIdentifierPair() {
        boolean emailPresent = email != null && !email.isBlank();
        boolean usernamePresent = username != null && !username.isBlank();
        return emailPresent || usernamePresent;
    }

}