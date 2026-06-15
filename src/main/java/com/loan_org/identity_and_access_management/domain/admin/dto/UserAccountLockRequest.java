package com.loan_org.identity_and_access_management.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UserAccountLockRequest(

        @NotBlank(message = "Please provide a reason why account should be locked")
        String reason

) {}
