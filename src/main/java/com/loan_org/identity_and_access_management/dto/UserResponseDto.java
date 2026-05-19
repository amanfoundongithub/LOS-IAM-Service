package com.loan_org.identity_and_access_management.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

/**
 * User response upon login
 */
@Data
@Builder
public class UserResponseDto {
    private String id;
    private String email;
    private String status;
    private Map<String, Object> attributes;
}