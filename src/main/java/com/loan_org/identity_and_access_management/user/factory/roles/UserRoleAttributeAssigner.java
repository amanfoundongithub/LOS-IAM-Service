package com.loan_org.identity_and_access_management.user.factory.roles;

import java.util.Map;

import com.loan_org.identity_and_access_management.user.entity.UserRole;

public interface UserRoleAttributeAssigner {
    UserRole getRole();
    Map<String, Object> assign();
}
