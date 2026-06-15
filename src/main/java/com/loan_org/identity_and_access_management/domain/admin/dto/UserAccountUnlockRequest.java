package com.loan_org.identity_and_access_management.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UserAccountUnlockRequest(

        @NotBlank(message = "Reason is required")
        String reason

){}
