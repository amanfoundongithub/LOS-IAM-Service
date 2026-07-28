package com.loan_org.identity_and_access_management.auth.register;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import com.loan_org.identity_and_access_management.userEntity.entity.UserRole;

/**
 * Represents a user registration request.
 *
 * @author Aman Raj
 * @since 1.0.0
 */
public record UserRegistrationRequest(

        /**
         * User email address.
         */
        @NotBlank(message = "Email is required.")
        @Email(message = "Please provide a valid email address.")
        String email,

        /**
         * Unique username.
         */
        @NotBlank(message = "Username is required.")
        @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters.")
        @Pattern(
                regexp = "^[a-zA-Z0-9_.-]+$",
                message = "Username may only contain letters, numbers, '_', '-' and '.'."
        )
        String username,

        /**
         * Plain-text password.
         */
        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!?.*()_\\-])[A-Za-z\\d@#$%^&+=!?.*()_\\-]{8,128}$",
                message = "Password must contain an uppercase letter, lowercase letter, digit and special character."
        )
        String password,

        /**
         * User role.
         */
        @NotNull(message = "Role is required.")
        UserRole role,

        /**
         * Maximum signing authority.
         */
        @PositiveOrZero(message = "Signing limit cannot be negative.")
        BigDecimal signingLimit

) {
}