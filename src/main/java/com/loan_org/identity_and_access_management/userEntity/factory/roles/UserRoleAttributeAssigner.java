package com.loan_org.identity_and_access_management.userEntity.factory.roles;

import java.util.Map;

import com.loan_org.identity_and_access_management.userEntity.entity.UserRole;

public interface UserRoleAttributeAssigner {
    UserRole getRole();
    Map<String, Object> assign();
}
