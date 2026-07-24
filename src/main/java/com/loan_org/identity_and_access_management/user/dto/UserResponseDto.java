package com.loan_org.identity_and_access_management.user.dto; // Updated package location

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

import com.loan_org.identity_and_access_management.user.entity.UserStatus;

/**
 * Data Transfer Object representing a sanitized profile view of a user account.
 * Excludes sensitive security blocks to prevent data leaks.
 *
 * @author Aman Raj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private String id;
    private String email;
    private String username;
    private UserStatus status;
    private Map<String, Object> attributes;
}