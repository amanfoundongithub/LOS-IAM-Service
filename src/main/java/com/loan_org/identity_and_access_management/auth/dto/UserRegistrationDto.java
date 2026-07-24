package com.loan_org.identity_and_access_management.auth.dto;

import com.loan_org.identity_and_access_management.user.entity.UserRole;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Email is required field")
    @Email(message = "Please provide valid email")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "Your role is required, please provide a valid role")
    private UserRole role;

    @PositiveOrZero(message = "Signing limit cannot be negative")
    private Double signingLimit;
}
