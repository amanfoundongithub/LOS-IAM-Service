package com.loan_org.identity_and_access_management.admin.model.user_search;

import com.loan_org.identity_and_access_management.domain.user.entity.UserRole;
import lombok.Builder;

@Builder
public record UserSearchAttributes(
        int page,
        int size,
        String sortBy,
        String sortDir,
        UserRole role,
        String status
) {}
