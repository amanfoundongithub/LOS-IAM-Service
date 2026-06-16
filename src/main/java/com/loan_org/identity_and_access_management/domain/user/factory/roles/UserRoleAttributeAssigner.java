package com.loan_org.identity_and_access_management.domain.user.factory.roles;

import com.loan_org.identity_and_access_management.domain.user.entity.UserRole;

import java.util.Map;

public interface UserRoleAttributeAssigner {
    UserRole getRole();
    Map<String, Object> assign();
}
