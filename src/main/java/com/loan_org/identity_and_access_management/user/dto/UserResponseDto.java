package com.loan_org.identity_and_access_management.user.dto;

import com.loan_org.identity_and_access_management.user.entity.UserStatus;

import lombok.Builder;

import java.util.Map;

/**
 * Represents a sanitized view of a user account returned to clients.
 *
 * <p>
 * Excludes sensitive security information such as password hashes,
 * MFA secrets, and account protection metadata.
 * </p>
 *
 * @author Aman Raj
 * @since 1.0.0
 */
@Builder
public record UserResponseDto(

        /**
         * Unique user identifier.
         */
        String id,

        /**
         * Registered email address.
         */
        String email,

        /**
         * Unique username.
         */
        String username,

        /**
         * Current account status.
         */
        UserStatus status,

        /**
         * User access attributes (role, permissions, etc.).
         */
        Map<String, Object> attributes

) {
}